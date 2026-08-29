import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import type { MusicaListagem } from '../../models/MusicaListagem';
import type { PaginaResponse } from '../../models/PaginaResponse';
import type { PerfilResponse } from '../../models/Perfil';
import { CatalogoService } from '../../services/catalogo';
import { MusicaService } from '../../services/musica';
import { PerfilService } from '../../services/perfil';
import { Perfil } from './perfil';

describe('Perfil', () => {
  let component: Perfil;
  let fixture: ComponentFixture<Perfil>;
  let perfilServiceMock: {
    obter: ReturnType<typeof vi.fn>;
    atualizar: ReturnType<typeof vi.fn>;
  };
  let musicaServiceMock: {
    pesquisar: ReturnType<typeof vi.fn>;
    buscarPorId: ReturnType<typeof vi.fn>;
  };

  const perfil: PerfilResponse = {
    idUsuario: 1,
    username: 'analiz',
    nome: 'Ana Liz Novaes',
    dataCadastro: '2026-01-01T12:00:00Z',
    role: 'USER',
    fotoUrl: 'https://exemplo.com/foto.jpg',
    bannerUrl: 'https://exemplo.com/banner.jpg',
    biografia: 'Entre pop brasileiro e descobertas independentes.',
    fraseDestaque: 'Uma trilha para cada fase.',
    tipoDestaquePrincipal: 'MUSICA',
    artistaDestaque: {
      tipo: 'ARTISTA', id: 5, titulo: 'Marina Sena',
      subtitulo: 'Artista', imagemUrl: null
    },
    musicaDestaque: {
      tipo: 'MUSICA', id: 10, titulo: 'Por Supuesto',
      subtitulo: 'Marina Sena', imagemUrl: null
    },
    albumDestaque: null
  };

  beforeEach(async () => {
    perfilServiceMock = {
      obter: vi.fn().mockReturnValue(of(perfil)),
      atualizar: vi.fn().mockReturnValue(of({ ...perfil, nome: 'Ana' }))
    };
    musicaServiceMock = {
      pesquisar: vi.fn().mockReturnValue(of(paginaMusicas([]))),
      buscarPorId: vi.fn().mockReturnValue(of(musicaFavorita()))
    };

    const catalogoServiceMock = {
      listarArtistas: vi.fn().mockReturnValue(of([])),
      listarAlbuns: vi.fn().mockReturnValue(of([]))
    };

    await TestBed.configureTestingModule({
      imports: [Perfil],
      providers: [
        provideRouter([]),
        { provide: PerfilService, useValue: perfilServiceMock },
        { provide: CatalogoService, useValue: catalogoServiceMock },
        { provide: MusicaService, useValue: musicaServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Perfil);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => vi.restoreAllMocks());

  it('deve exibir o nome sem revelar o email', () => {
    expect(fixture.nativeElement.textContent).toContain('Ana Liz Novaes');
    expect(fixture.nativeElement.textContent).not.toContain('@example');
  });

  it('deve usar o ícone existente e ocultar rótulo e nota quando há banner', () => {
    const marca = fixture.nativeElement.querySelector('.marca img');

    expect(marca.getAttribute('src')).toBe('/favicon.svg');
    expect(fixture.nativeElement.querySelector('.tipo-perfil')).toBeNull();
    expect(fixture.nativeElement.querySelector('.banner-nota')).toBeNull();
  });

  it('deve mostrar a nota do banner somente quando não há imagem escolhida', () => {
    component.perfil.set({ ...perfil, bannerUrl: null });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.banner-nota')).not.toBeNull();
  });

  it('deve colocar a escolha principal no cartão de destaque', () => {
    expect(component.destaquePrincipal()?.titulo).toBe('Por Supuesto');
    expect(component.favoritosSecundarios()[0].titulo).toBe('Marina Sena');
  });

  it('deve abrir o editor preenchido com os dados atuais', () => {
    component.abrirEdicao();

    expect(component.editando()).toBe(true);
    expect(component.formulario.controls.username.value).toBe('analiz');
    expect(component.formulario.controls.fotoUrl.value)
      .toBe('https://exemplo.com/foto.jpg');
  });

  it('deve impedir link de imagem sem protocolo http', () => {
    component.formulario.controls.fotoUrl.setValue('exemplo.com/foto.jpg');
    expect(component.formulario.controls.fotoUrl.hasError('urlHttp')).toBe(true);
  });

  it('deve pesquisar músicas no backend e carregar as próximas páginas', () => {
    const primeira = musicaFavorita();
    const segunda = { ...musicaFavorita(), id: 11, titulo: 'Love of My Life' };

    musicaServiceMock.pesquisar
      .mockReturnValueOnce(of(paginaMusicas([primeira], 0, 2, 2)))
      .mockReturnValueOnce(of(paginaMusicas([segunda], 0, 2, 2)))
      .mockReturnValueOnce(of(paginaMusicas([
        { ...musicaFavorita(), id: 12, titulo: 'Somebody to Love' }
      ], 1, 2, 2)));

    component.abrirEdicao();
    component.atualizarBuscaMusica({
      currentTarget: { value: 'Love' }
    } as unknown as Event);
    component.pesquisarMusicas();
    component.carregarMaisMusicas();

    expect(musicaServiceMock.pesquisar).toHaveBeenNthCalledWith(
      2,
      { titulo: 'Love' },
      0,
      25,
      'titulo,asc'
    );
    expect(musicaServiceMock.pesquisar).toHaveBeenNthCalledWith(
      3,
      { titulo: 'Love' },
      1,
      25,
      'titulo,asc'
    );
    expect(component.musicas().map(musica => musica.id))
      .toEqual([10, 11, 12]);
  });

  it('deve salvar o perfil sem enviar strings opcionais vazias', () => {
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => undefined);
    component.abrirEdicao();
    component.formulario.patchValue({
      nome: 'Ana',
      username: '',
      fotoUrl: '',
      bannerUrl: '',
      tipoDestaquePrincipal: 'MUSICA'
    });

    component.salvar();

    expect(perfilServiceMock.atualizar).toHaveBeenCalledWith(
      expect.objectContaining({
        nome: 'Ana',
        username: null,
        fotoUrl: null,
        bannerUrl: null,
        idMusicaDestaque: 10,
        tipoDestaquePrincipal: 'MUSICA'
      })
    );
    expect(component.editando()).toBe(false);
  });

  function musicaFavorita(): MusicaListagem {
    return {
      id: 10,
      titulo: 'Por Supuesto',
      duracaoSegundos: 180,
      anoLancamento: 2021,
      artistaPrincipal: { id: 5, nome: 'Marina Sena' },
      album: null,
      artistasParticipantes: [],
      generos: []
    };
  }

  function paginaMusicas(
    itens: MusicaListagem[],
    paginaAtual = 0,
    totalPaginas = 1,
    totalItens = itens.length
  ): PaginaResponse<MusicaListagem> {
    return {
      itens,
      paginaAtual,
      tamanhoPagina: 25,
      totalItens,
      totalPaginas
    };
  }
});
