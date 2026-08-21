import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { Catalogo } from './catalogo';
import { MusicaListagem } from '../../models/MusicaListagem';
import { PlaylistResponse } from '../../models/PlaylistResponse';

describe('Catalogo', () => {
  let component: Catalogo;
  let fixture: ComponentFixture<Catalogo>;
  let httpMock: HttpTestingController;

  const musicasUrl = 'http://localhost:8080/api/musicas';
  const playlistsUrl = 'http://localhost:8080/api/playlists';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Catalogo],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: convertToParamMap({ id: '1' }) },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Catalogo);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function musicaDeExemplo(): MusicaListagem {
    return {
      id: 5,
      titulo: 'Bohemian Rhapsody',
      duracaoSegundos: 354,
      anoLancamento: 1975,
      artistaPrincipal: { id: 1, nome: 'Queen' },
      album: { id: 1, titulo: 'A Night at the Opera', anoLancamento: 1975, capaUrl: null },
      generos: [{ id: 1, nome: 'Rock' }],
    };
  }

  function mockarRespostasIniciais(
    musicasPlaylist: PlaylistResponse['musicas'] = []
  ): void {
    httpMock.expectOne(`${musicasUrl}?page=0&size=100`).flush({
      itens: [musicaDeExemplo()],
      paginaAtual: 0,
      tamanhoPagina: 100,
      totalItens: 1,
      totalPaginas: 1,
    });

    httpMock.expectOne(`${playlistsUrl}/1`).flush({
      id: 1,
      nome: 'Playlist Teste',
      descricao: '',
      musicas: musicasPlaylist
    });
  }

  function criarEventoDeBusca(valor: string): Event {
    const campo = document.createElement('input');
    campo.value = valor;

    return {
      target: campo
    } as unknown as Event;
  }

  it('deve carregar o catálogo de músicas usando o id da playlist da rota', () => {
    fixture.detectChanges();
    mockarRespostasIniciais();

    expect(component.playlistId).toBe(1);
    expect(component.musicas()).toHaveLength(1);
  });

  it('deve atualizar as músicas ao pesquisar por título', () => {
    component.atualizarBusca(
      criarEventoDeBusca('  Queen  ')
    );

    const requisicao = httpMock.expectOne(
      `${musicasUrl}?titulo=Queen&page=0&size=100`
    );

    requisicao.flush({
      itens: [],
      paginaAtual: 0,
      tamanhoPagina: 100,
      totalItens: 0,
      totalPaginas: 0
    });

    expect(component.musicas()).toEqual([]);
  });

  it('deve informar erro ao carregar catálogo e playlist', () => {
    fixture.detectChanges();

    const requisicaoCatalogo = httpMock.expectOne(
      `${musicasUrl}?page=0&size=100`
    );
    const requisicaoPlaylist = httpMock.expectOne(
      `${playlistsUrl}/1`
    );

    requisicaoCatalogo.flush({
      itens: [musicaDeExemplo()],
      paginaAtual: 0,
      tamanhoPagina: 100,
      totalItens: 1,
      totalPaginas: 1
    });

    requisicaoPlaylist.flush(
      {},
      {
        status: 500,
        statusText: 'Internal Server Error'
      }
    );

    expect(component.mensagemErro()).toBe(
      'Erro ao carregar os dados do catálogo e da playlist.'
    );
  });

  it('deve adicionar a música na playlist e marcar como adicionada', () => {
    fixture.detectChanges();
    mockarRespostasIniciais();

    component.adicionarMusica(5);
    httpMock
      .expectOne(`${playlistsUrl}/1/musicas/5`)
      .flush(null, { status: 204, statusText: 'No Content' });

    expect(component.musicasAdicionadas()[5]).toBe(true);
    expect(component.loadingAdicionar()[5]).toBeFalsy();
    expect(component.mensagemSucesso()).toBeTruthy();
  });

  it('não deve enviar uma segunda requisição para uma música já adicionada', () => {
    fixture.detectChanges();
    mockarRespostasIniciais();

    component.adicionarMusica(5);
    httpMock
      .expectOne(`${playlistsUrl}/1/musicas/5`)
      .flush(null, { status: 204, statusText: 'No Content' });

    component.adicionarMusica(5);
    httpMock.expectNone(`${playlistsUrl}/1/musicas/5`);
  });

  it('não deve remover música que não esteja na playlist', () => {
    fixture.detectChanges();
    mockarRespostasIniciais();

    component.removerMusica(5);

    httpMock.expectNone(`${playlistsUrl}/1/musicas/5`);
  });

  it('deve remover música e atualizar o estado local', () => {
    fixture.detectChanges();
    mockarRespostasIniciais([
      {
        id: 5,
        titulo: 'Bohemian Rhapsody',
        artista: 'Queen'
      }
    ]);

    component.removerMusica(5);

    const requisicao = httpMock.expectOne(
      `${playlistsUrl}/1/musicas/5`
    );

    expect(requisicao.request.method).toBe('DELETE');

    requisicao.flush(
      null,
      {
        status: 204,
        statusText: 'No Content'
      }
    );

    expect(component.musicasAdicionadas()[5]).toBe(false);
    expect(component.loadingAdicionar()[5]).toBe(false);
    expect(component.mensagemSucesso()).toBe(
      'Música removida da playlist com sucesso!'
    );
  });

  it('deve liberar a música quando a remoção falhar', () => {
    fixture.detectChanges();
    mockarRespostasIniciais([
      {
        id: 5,
        titulo: 'Bohemian Rhapsody',
        artista: 'Queen'
      }
    ]);

    component.removerMusica(5);

    httpMock
      .expectOne(`${playlistsUrl}/1/musicas/5`)
      .flush(
        {},
        {
          status: 500,
          statusText: 'Internal Server Error'
        }
      );

    expect(component.loadingAdicionar()[5]).toBe(false);
    expect(component.musicasAdicionadas()[5]).toBe(true);
    expect(component.mensagemErro()).toBe(
      'Não foi possível remover a música.'
    );
  });

  it('deve mostrar mensagem de erro e liberar o botão quando a requisição falha', () => {
    fixture.detectChanges();
    mockarRespostasIniciais();

    component.adicionarMusica(5);
    httpMock
      .expectOne(`${playlistsUrl}/1/musicas/5`)
      .flush({ message: 'erro' }, { status: 500, statusText: 'Internal Server Error' });

    expect(component.mensagemErro()).toBeTruthy();
    expect(component.loadingAdicionar()[5]).toBeFalsy();
    expect(component.musicasAdicionadas()[5]).toBeFalsy();
  });

  const cenariosDeErro = [
    {
      status: 401,
      statusText: 'Unauthorized',
      mensagemEsperada:
        'Sessão expirada ou não autenticada. Faça login novamente.'
    },
    {
      status: 403,
      statusText: 'Forbidden',
      mensagemEsperada:
        'Você não tem permissão para alterar esta playlist.'
    },
    {
      status: 404,
      statusText: 'Not Found',
      mensagemEsperada:
        'Música ou Playlist não encontrada.'
    },
    {
      status: 409,
      statusText: 'Conflict',
      mensagemEsperada:
        'Esta música já está na sua playlist!'
    }
  ];

  for (const cenario of cenariosDeErro) {
    it(`deve tratar erro ${cenario.status} durante a busca`, () => {
      component.atualizarBusca(
        criarEventoDeBusca('Queen')
      );

      httpMock
        .expectOne(
          `${musicasUrl}?titulo=Queen&page=0&size=100`
        )
        .flush(
          {},
          {
            status: cenario.status,
            statusText: cenario.statusText
          }
        );

      expect(component.mensagemErro()).toBe(
        cenario.mensagemEsperada
      );
    });
  }
});
