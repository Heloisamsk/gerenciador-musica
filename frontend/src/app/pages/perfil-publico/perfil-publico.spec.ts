import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  provideRouter
} from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import type { PerfilResponse } from '../../models/Perfil';
import { PerfilService } from '../../services/perfil';
import { SeguidorService } from '../../services/seguidor';
import { PerfilPublico } from './perfil-publico';

describe('PerfilPublico', () => {
  let component: PerfilPublico;
  let fixture: ComponentFixture<PerfilPublico>;
  let perfilServiceMock: { obterPorId: ReturnType<typeof vi.fn> };
  let seguidorServiceMock: {
    seguirUsuario: ReturnType<typeof vi.fn>;
    deixarDeSeguirUsuario: ReturnType<typeof vi.fn>;
    seguirArtista: ReturnType<typeof vi.fn>;
    deixarDeSeguirArtista: ReturnType<typeof vi.fn>;
  };

  const perfil: PerfilResponse = {
    idUsuario: 2,
    username: 'joaosilva',
    nome: 'João Silva',
    dataCadastro: '2026-01-01T12:00:00Z',
    role: 'USER',
    fotoUrl: null,
    bannerUrl: null,
    biografia: null,
    fraseDestaque: null,
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
    totalAlbunsAvaliadas: 1,
    totalSeguidores: 3,
    totalSeguindo: 2,
    perfilDoUsuarioAutenticado: false,
    seguindoPorUsuarioAutenticado: true
  };

  beforeEach(() => {
    perfilServiceMock = {
      obterPorId: vi.fn().mockReturnValue(of(perfil))
    };
    seguidorServiceMock = {
      seguirUsuario: vi.fn().mockReturnValue(of(undefined)),
      deixarDeSeguirUsuario: vi.fn().mockReturnValue(of(undefined)),
      seguirArtista: vi.fn().mockReturnValue(of(undefined)),
      deixarDeSeguirArtista: vi.fn().mockReturnValue(of(undefined))
    };
  });

  afterEach(() => vi.restoreAllMocks());

  async function configurarComId(id: string): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [PerfilPublico],
      providers: [
        provideRouter([]),
        { provide: PerfilService, useValue: perfilServiceMock },
        { provide: SeguidorService, useValue: seguidorServiceMock },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id }) } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PerfilPublico);
    component = fixture.componentInstance;
  }

  it('deve carregar e exibir o perfil público', async () => {
    await configurarComId('2');
    fixture.detectChanges();

    expect(perfilServiceMock.obterPorId).toHaveBeenCalledWith(2);
    expect(component.carregando()).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('João Silva');
    expect(fixture.nativeElement.textContent).toContain('@joaosilva');
  });

  it('deve exibir o botão de seguir quando não é o próprio perfil', async () => {
    await configurarComId('2');
    fixture.detectChanges();

    const botao = fixture.nativeElement.querySelector('app-seguir-botao');
    expect(botao).not.toBeNull();
  });

  it('não deve exibir o botão de seguir no próprio perfil', async () => {
    perfilServiceMock.obterPorId.mockReturnValue(
      of({ ...perfil, perfilDoUsuarioAutenticado: true })
    );
    await configurarComId('2');
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('app-seguir-botao')
    ).toBeNull();
  });

  it('não deve chamar a API quando o id da rota é inválido', async () => {
    await configurarComId('abc');
    fixture.detectChanges();

    expect(perfilServiceMock.obterPorId).not.toHaveBeenCalled();
    expect(component.carregando()).toBe(false);
    expect(component.mensagemErro()).toBe(
      'O identificador do perfil é inválido.'
    );
  });

  it('não deve chamar a API quando o id da rota é zero ou negativo', async () => {
    await configurarComId('0');
    fixture.detectChanges();

    expect(perfilServiceMock.obterPorId).not.toHaveBeenCalled();
    expect(component.mensagemErro()).toBe(
      'O identificador do perfil é inválido.'
    );
  });

  it('deve exibir a mensagem de erro vinda da API', async () => {
    perfilServiceMock.obterPorId.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        error: { message: 'Usuário não encontrado.' }
      }))
    );
    await configurarComId('99');
    fixture.detectChanges();

    expect(component.carregando()).toBe(false);
    expect(component.perfil()).toBeNull();
    expect(component.mensagemErro()).toBe('Usuário não encontrado.');
    expect(fixture.nativeElement.textContent)
      .toContain('Não foi possível abrir o perfil');
  });

  it('deve exibir mensagem padrão quando o erro não é HTTP', async () => {
    perfilServiceMock.obterPorId.mockReturnValue(
      throwError(() => new Error('falha de rede'))
    );
    await configurarComId('2');
    fixture.detectChanges();

    expect(component.mensagemErro())
      .toBe('Não foi possível carregar o perfil.');
  });

  it('deve exibir mensagem padrão quando o erro HTTP não possui corpo', async () => {
    perfilServiceMock.obterPorId.mockReturnValue(
      throwError(() => new HttpErrorResponse({}))
    );
    await configurarComId('2');
    fixture.detectChanges();

    expect(component.mensagemErro())
      .toBe('Não foi possível carregar o perfil.');
  });

  it('deve resolver o destaque principal por tipo', async () => {
    await configurarComId('2');
    fixture.detectChanges();

    expect(component.destaquePrincipal()?.titulo).toBe('Por Supuesto');
  });

  it('deve usar ARTISTA como tipo padrão quando não há tipo definido', async () => {
    perfilServiceMock.obterPorId.mockReturnValue(of({
      ...perfil,
      tipoDestaquePrincipal: null,
      artistaDestaque: {
        tipo: 'ARTISTA', id: 5, titulo: 'Marina Sena',
        subtitulo: 'Artista', imagemUrl: null
      }
    }));
    await configurarComId('2');
    fixture.detectChanges();

    expect(component.destaquePrincipal()?.titulo).toBe('Marina Sena');
  });

  it('deve retornar null quando não há perfil carregado', async () => {
    await configurarComId('2');
    expect(component.destaquePrincipal()).toBeNull();
  });

  it('deve agrupar os favoritos por categoria', async () => {
    await configurarComId('2');
    fixture.detectChanges();

    const grupos = component.gruposFavoritos();
    expect(grupos).toHaveLength(3);
    expect(grupos[0].itens[0].titulo).toBe('Marina Sena');
    expect(grupos[1].itens).toEqual([]);
    expect(grupos[2].itens).toEqual([]);
  });

  it('deve montar a rota de cada tipo de item', async () => {
    await configurarComId('2');

    expect(component.rotaItem({
      tipo: 'ARTISTA', id: 5, titulo: '', subtitulo: '', imagemUrl: null
    })).toEqual(['/', 'artistas', '5']);
    expect(component.rotaItem({
      tipo: 'MUSICA', id: 10, titulo: '', subtitulo: '', imagemUrl: null
    })).toEqual(['/', 'musicas', '10']);
    expect(component.rotaItem({
      tipo: 'ALBUM', id: 20, titulo: '', subtitulo: '', imagemUrl: null
    })).toEqual(['/', 'albuns', '20']);
  });

  it('deve rotular cada tipo de destaque', async () => {
    await configurarComId('2');

    expect(component.rotuloTipo('ARTISTA')).toBe('Artista em destaque');
    expect(component.rotuloTipo('MUSICA')).toBe('Música em destaque');
    expect(component.rotuloTipo('ALBUM')).toBe('Álbum em destaque');
  });

  it('deve escolher a imagem alternativa correta por tipo', async () => {
    await configurarComId('2');

    expect(component.imagemAlternativa('ARTISTA')).toBe('/avatar-artista.png');
    expect(component.imagemAlternativa('MUSICA')).toBe('/capa-padrao.png');
    expect(component.imagemAlternativa('ALBUM')).toBe('/capa-padrao.png');
  });

  it('deve corrigir a imagem quebrada apenas uma vez', async () => {
    await configurarComId('2');

    const imagem = document.createElement('img');
    imagem.src = 'https://exemplo.com/quebrada.jpg';
    const evento = { currentTarget: imagem } as unknown as Event;

    component.corrigirImagem(evento, '/avatar-padrao.svg');
    expect(imagem.src).toContain('/avatar-padrao.svg');

    const srcAposCorrecao = imagem.src;
    component.corrigirImagem(evento, '/avatar-padrao.svg');
    expect(imagem.src).toBe(srcAposCorrecao);
  });
});
