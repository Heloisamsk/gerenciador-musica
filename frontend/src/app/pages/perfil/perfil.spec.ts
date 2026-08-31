import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import type { MusicaListagem } from '../../models/MusicaListagem';
import type { PaginaResponse } from '../../models/PaginaResponse';
import type { PerfilResponse } from '../../models/Perfil';
import type { PlaylistResponse } from '../../models/PlaylistResponse';
import type { Review } from '../../models/Review';
import { CatalogoService } from '../../services/catalogo';
import { MusicaService } from '../../services/musica';
import { PerfilService } from '../../services/perfil';
import { PlaylistService } from '../../services/playlist';
import { ReviewService } from '../../services/review';
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
  let catalogoServiceMock: {
    listarArtistas: ReturnType<typeof vi.fn>;
    listarAlbuns: ReturnType<typeof vi.fn>;
  };
  let reviewServiceMock: {
    listarMinhas: ReturnType<typeof vi.fn>;
  };
  let playlistServiceMock: {
    listarMinhas: ReturnType<typeof vi.fn>;
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
    artistaDestaque: null,
    musicaDestaque: {
      tipo: 'MUSICA', id: 10, titulo: 'Por Supuesto',
      subtitulo: 'Marina Sena', imagemUrl: null
    },
    albumDestaque: null,
    artistasFavoritos: [{
      tipo: 'ARTISTA', id: 5, titulo: 'Marina Sena',
      subtitulo: 'Artista', imagemUrl: null
    }],
    albunsFavoritos: [],
    musicasFavoritas: [],
    totalMusicasAvaliadas: 4,
    totalAlbunsAvaliadas: 1
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

    catalogoServiceMock = {
      listarArtistas: vi.fn().mockReturnValue(of([
        {
          idArtista: 5,
          nome: 'Marina Sena',
          nomeCompleto: 'Marina Sena',
          descricao: 'Cantora e compositora brasileira.',
          fotoPerfilUrl: null
        },
        {
          idArtista: 6,
          nome: 'Liniker',
          nomeCompleto: 'Liniker de Barros Ferreira Campos',
          descricao: 'Cantora e compositora brasileira.',
          fotoPerfilUrl: null
        }
      ])),
      listarAlbuns: vi.fn().mockReturnValue(of([
        {
          idAlbum: 20,
          titulo: 'De Primeira',
          anoLancamento: 2021,
          capaUrl: null,
          artista: {
            id: 5,
            nome: 'Marina Sena',
            nomeCompleto: 'Marina Sena',
            descricao: 'Cantora e compositora brasileira.',
            fotoPerfilUrl: null
          }
        }
      ]))
    };

    reviewServiceMock = {
      listarMinhas: vi.fn().mockReturnValue(of(paginaReviews([])))
    };
    playlistServiceMock = {
      listarMinhas: vi.fn().mockReturnValue(of([]))
    };

    await TestBed.configureTestingModule({
      imports: [Perfil],
      providers: [
        provideRouter([]),
        { provide: PerfilService, useValue: perfilServiceMock },
        { provide: CatalogoService, useValue: catalogoServiceMock },
        { provide: MusicaService, useValue: musicaServiceMock },
        { provide: ReviewService, useValue: reviewServiceMock },
        { provide: PlaylistService, useValue: playlistServiceMock }
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
    expect(component.gruposFavoritos()[0].itens[0].titulo)
      .toBe('Marina Sena');
    expect(fixture.nativeElement.querySelector('.contador')).toBeNull();
  });

  it('deve abrir o editor preenchido com os dados atuais', () => {
    component.abrirEdicao();
    fixture.detectChanges();

    expect(component.editando()).toBe(true);
    expect(component.formulario.controls.username.value).toBe('analiz');
    expect(component.formulario.controls.fotoUrl.value)
      .toBe('https://exemplo.com/foto.jpg');
    expect(fixture.nativeElement.querySelector('.editor').tagName)
      .toBe('DIALOG');
  });

  it('deve carregar e exibir os artistas no seletor de favoritos', () => {
    component.abrirEdicao();
    fixture.detectChanges();

    const opcoes = fixture.nativeElement.querySelectorAll(
      '#favoritos-artistas + .opcoes-favoritos .opcao-favorito'
    );

    expect(catalogoServiceMock.listarArtistas).toHaveBeenCalledOnce();
    expect(opcoes).toHaveLength(2);
    expect(opcoes[0].textContent).toContain('Marina Sena');
    expect(opcoes[1].textContent).toContain('Liniker');
    expect(opcoes[0].querySelector('input').type).toBe('checkbox');
  });

  it('deve abrir o editor quando a API omite coleções vazias', () => {
    component.perfil.set({
      nome: 'Perfil novo',
      role: 'USER'
    } as PerfilResponse);

    expect(() => component.abrirEdicao()).not.toThrow();
    fixture.detectChanges();

    expect(component.editando()).toBe(true);
    expect(component.formulario.controls.idsArtistasFavoritos.value)
      .toEqual([]);
    expect(component.formulario.controls.idsAlbunsFavoritos.value)
      .toEqual([]);
    expect(component.formulario.controls.idsMusicasFavoritas.value)
      .toEqual([]);
    expect(catalogoServiceMock.listarArtistas).toHaveBeenCalledOnce();
  });

  it('deve fechar o editor pela tecla Escape', () => {
    component.abrirEdicao();
    fixture.detectChanges();

    const editor = fixture.nativeElement.querySelector('.editor');
    editor.dispatchEvent(new KeyboardEvent('keydown', {
      key: 'Escape',
      bubbles: true
    }));

    expect(component.editando()).toBe(false);
  });

  it('deve impedir link de imagem sem protocolo http', () => {
    component.formulario.controls.fotoUrl.setValue('exemplo.com/foto.jpg');
    expect(component.formulario.controls.fotoUrl.hasError('urlHttp')).toBe(true);
  });

  it('deve manter o destaque principal fora da lista de favoritos', () => {
    component.abrirEdicao();
    component.formulario.patchValue({
      tipoDestaquePrincipal: 'ARTISTA',
      idArtistaDestaque: 5,
      idsArtistasFavoritos: [5]
    });

    component.aoAlterarDestaque();

    expect(component.formulario.controls.idsArtistasFavoritos.value)
      .toEqual([]);
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
        tipoDestaquePrincipal: 'MUSICA',
        idsArtistasFavoritos: [5],
        idsAlbunsFavoritos: [],
        idsMusicasFavoritas: []
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

  function paginaReviews(itens: Review[]): PaginaResponse<Review> {
    return {
      itens,
      paginaAtual: 0,
      tamanhoPagina: 5,
      totalItens: itens.length,
      totalPaginas: 1
    };
  }

  function playlistDeExemplo(): PlaylistResponse {
    return {
      id: 1,
      nome: 'Favoritas',
      descricao: '',
      capaUrl: null,
      musicas: [{ id: 10, titulo: 'Por Supuesto', artista: 'Marina Sena', capaUrl: null }]
    };
  }

  it('deve exibir as estatísticas de reviews do perfil', () => {
    const estatisticas = fixture.nativeElement.querySelectorAll(
      '.estatisticas-reviews dd'
    );

    expect(estatisticas[0].textContent.trim()).toBe('4');
    expect(estatisticas[1].textContent.trim()).toBe('1');
  });

  it('deve exibir as reviews recentes do perfil', () => {
    reviewServiceMock.listarMinhas.mockReturnValue(of(paginaReviews([{
      idReview: 3,
      autor: { id: 1, nome: 'Ana Liz Novaes' },
      alvo: { tipo: 'MUSICA', id: 10, titulo: 'Por Supuesto', artista: 'Marina Sena', capaUrl: null },
      nota: 5,
      texto: null,
      criadaEm: '2026-01-01T00:00:00Z',
      atualizadaEm: '2026-01-01T00:00:00Z',
      minhaReview: true
    }])));

    fixture = TestBed.createComponent(Perfil);
    fixture.detectChanges();

    const cartoes = fixture.nativeElement.querySelectorAll(
      '.reviews-lista-horizontal .review-card'
    );
    expect(cartoes.length).toBe(1);
  });

  it('deve exibir mensagem de estado vazio quando não há reviews', () => {
    expect(fixture.nativeElement.querySelector('.reviews-recentes-area').textContent)
      .toContain('ainda não fez nenhuma review');
  });

  it('deve exibir as playlists do usuário', () => {
    playlistServiceMock.listarMinhas.mockReturnValue(of([playlistDeExemplo()]));

    fixture = TestBed.createComponent(Perfil);
    fixture.detectChanges();

    const cartoes = fixture.nativeElement.querySelectorAll('.playlist-mini-card');
    expect(cartoes.length).toBe(1);
    expect(cartoes[0].textContent).toContain('Favoritas');
  });

  it('deve exibir mensagem de estado vazio quando não há playlists', () => {
    expect(fixture.nativeElement.querySelector('.playlists-area').textContent)
      .toContain('ainda não criou nenhuma playlist');
  });
});
