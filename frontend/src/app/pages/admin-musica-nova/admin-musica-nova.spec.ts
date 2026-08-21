import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';

import { AdminMusicaNova } from './admin-musica-nova';
import { AlbumResponse } from '../../models/AlbumResponse';
import { ArtistaResponse } from '../../models/ArtistaResponse';
import { ArtistaResumo } from '../../models/ArtistaResumoModel';

describe('AdminMusicaNova', () => {
  let component: AdminMusicaNova;
  let fixture: ComponentFixture<AdminMusicaNova>;
  let httpMock: HttpTestingController;
  let router: Router;

  const apiMusicasUrl =
    'http://localhost:8080/api/admin/musicas';

  const apiArtistasUrl =
    'http://localhost:8080/api/artistas';

  const apiAlbunsUrl =
    'http://localhost:8080/api/albuns';

  const artistaMock: ArtistaResponse = {
    idArtista: 1,
    nome: 'Queen',
    nomeCompleto: 'Queen',
    descricao: 'Banda britânica de rock.',
    fotoPerfilUrl: null
  };

  const outroArtistaMock: ArtistaResponse = {
    idArtista: 2,
    nome: 'David Bowie',
    nomeCompleto: 'David Bowie',
    descricao: 'Cantor britânico.',
    fotoPerfilUrl: null
  };

  const artistaResumoMock: ArtistaResumo = {
    id: 1,
    nome: 'Queen',
    nomeCompleto: 'Queen',
    descricao: 'Banda britânica de rock.',
    fotoPerfilUrl: null
  };

  const albumMock: AlbumResponse = {
    idAlbum: 10,
    titulo: 'A Night at the Opera',
    anoLancamento: 1975,
    capaUrl: null,
    artista: artistaResumoMock
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminMusicaNova],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminMusicaNova);
    component = fixture.componentInstance;

    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture.detectChanges();

    const requisicaoArtistas =
      httpMock.expectOne(apiArtistasUrl);

    expect(requisicaoArtistas.request.method).toBe('GET');

    requisicaoArtistas.flush([
      artistaMock,
      outroArtistaMock
    ]);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function selecionarArtistaECarregarAlbuns(
    albuns: AlbumResponse[] = [albumMock]
  ): void {
    component.formularioMusica
      .controls['artistaPrincipalId']
      .setValue(artistaMock.idArtista);

    const requisicaoAlbuns = httpMock.expectOne(
      `${apiAlbunsUrl}?artistaId=${artistaMock.idArtista}`
    );

    expect(requisicaoAlbuns.request.method).toBe('GET');

    requisicaoAlbuns.flush(albuns);
  }

  function preencherFormularioValido(
    albumId: number | null = albumMock.idAlbum
  ): void {
    selecionarArtistaECarregarAlbuns();

    component.formularioMusica.patchValue({
      titulo: 'Bohemian Rhapsody',
      duracao: '354',
      genero: 'Rock',
      anoLancamento: '1975',
      albumId
    });
  }

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('não deve enviar requisição quando o formulário é inválido', () => {
    component.salvar();

    httpMock.expectNone(apiMusicasUrl);
  });

  it('deve carregar somente os álbuns do artista selecionado', () => {
    component.formularioMusica
      .controls['artistaPrincipalId']
      .setValue(artistaMock.idArtista);

    expect(component.carregandoAlbuns()).toBe(true);

    const requisicaoAlbuns = httpMock.expectOne(
      `${apiAlbunsUrl}?artistaId=1`
    );

    requisicaoAlbuns.flush([albumMock]);

    expect(component.albuns()).toEqual([albumMock]);
    expect(component.carregandoAlbuns()).toBe(false);
  });

  it('deve informar erro quando não conseguir carregar os álbuns', () => {
    component.formularioMusica
      .controls['artistaPrincipalId']
      .setValue(artistaMock.idArtista);

    httpMock
      .expectOne(`${apiAlbunsUrl}?artistaId=1`)
      .flush(
        {},
        {
          status: 500,
          statusText: 'Internal Server Error'
        }
      );

    expect(component.albuns()).toEqual([]);
    expect(component.erroAlbuns()).toBe(
      'Não foi possível carregar os álbuns desse artista.'
    );
    expect(component.carregandoAlbuns()).toBe(false);
  });

  it('não deve salvar enquanto música ou álbuns estiverem carregando', () => {
    component.carregando.set(true);

    component.salvar();

    httpMock.expectNone(apiMusicasUrl);

    component.carregando.set(false);
    component.carregandoAlbuns.set(true);

    component.salvar();

    httpMock.expectNone(apiMusicasUrl);

    component.carregandoAlbuns.set(false);
  });

  it('deve limpar o álbum ao trocar o artista', () => {
    selecionarArtistaECarregarAlbuns();

    component.formularioMusica
      .controls['albumId']
      .setValue(albumMock.idAlbum);

    component.formularioMusica
      .controls['artistaPrincipalId']
      .setValue(outroArtistaMock.idArtista);

    expect(
      component.formularioMusica.controls['albumId'].value
    ).toBeNull();

    expect(component.albuns()).toEqual([]);

    httpMock
      .expectOne(`${apiAlbunsUrl}?artistaId=2`)
      .flush([]);
  });

  it('deve montar o payload com o ID de um álbum existente', () => {
    preencherFormularioValido();

    component.salvar();

    const requisicao = httpMock.expectOne(apiMusicasUrl);

    expect(requisicao.request.method).toBe('POST');

    expect(requisicao.request.body).toEqual({
      titulo: 'Bohemian Rhapsody',
      duracaoSegundos: 354,
      anoLancamento: 1975,
      artistaPrincipalId: 1,
      artistasParticipantesIds: [],
      albumId: 10,
      generos: ['Rock']
    });

    requisicao.flush({ id: 1 });
  });

  it('deve permitir cadastrar uma música sem álbum', () => {
    preencherFormularioValido(null);

    component.salvar();

    const requisicao = httpMock.expectOne(apiMusicasUrl);

    expect(requisicao.request.body.albumId).toBeNull();

    requisicao.flush({ id: 1 });
  });

  it('não deve enviar álbum que não pertença à lista do artista', () => {
    preencherFormularioValido(999);

    component.salvar();

    httpMock.expectNone(apiMusicasUrl);

    expect(component.mensagemErro()).toContain('válidos');
  });

  it('deve navegar para a listagem quando o cadastro tem sucesso', () => {
    preencherFormularioValido();

    component.salvar();

    httpMock
      .expectOne(apiMusicasUrl)
      .flush({ id: 1 });

    expect(component.mensagemSucesso()).toBeTruthy();
    expect(component.carregando()).toBe(false);

    expect(router.navigate).toHaveBeenCalledWith([
      '/admin/banco/musicas'
    ]);
  });

  it('deve mostrar mensagem de conflito quando a música já está cadastrada', () => {
    preencherFormularioValido();

    component.salvar();

    httpMock
      .expectOne(apiMusicasUrl)
      .flush(
        {
          message: 'A música já está cadastrada.'
        },
        {
          status: 409,
          statusText: 'Conflict'
        }
      );

    expect(component.mensagemErro())
      .toContain('já está cadastrada');

    expect(component.carregando()).toBe(false);
  });

  const cenariosDeErro = [
    {
      status: 400,
      statusText: 'Bad Request',
      corpo: {
        message: 'Dados da música inválidos.'
      },
      mensagemEsperada: 'Dados da música inválidos.'
    },
    {
      status: 400,
      statusText: 'Bad Request',
      corpo: null,
      mensagemEsperada:
        'Dados inválidos. Verifique os campos.'
    },
    {
      status: 401,
      statusText: 'Unauthorized',
      corpo: {},
      mensagemEsperada:
        'Não autorizado. Faça login novamente.'
    },
    {
      status: 403,
      statusText: 'Forbidden',
      corpo: {},
      mensagemEsperada:
        'Acesso negado. Você não tem permissão.'
    },
    {
      status: 404,
      statusText: 'Not Found',
      corpo: {
        message: 'O álbum informado não foi encontrado.'
      },
      mensagemEsperada:
        'O álbum informado não foi encontrado.'
    },
    {
      status: 404,
      statusText: 'Not Found',
      corpo: null,
      mensagemEsperada:
        'O artista ou o álbum selecionado não foi encontrado.'
    },
    {
      status: 500,
      statusText: 'Internal Server Error',
      corpo: {},
      mensagemEsperada:
        'Erro inesperado ao cadastrar a música.'
    }
  ];

  for (const cenario of cenariosDeErro) {
    it(`deve tratar resposta de erro ${cenario.status}: ${cenario.mensagemEsperada}`, () => {
      preencherFormularioValido();

      component.salvar();

      httpMock
        .expectOne(apiMusicasUrl)
        .flush(
          cenario.corpo,
          {
            status: cenario.status,
            statusText: cenario.statusText
          }
        );

      expect(component.mensagemErro()).toBe(
        cenario.mensagemEsperada
      );
      expect(component.carregando()).toBe(false);
    });
  }
});
