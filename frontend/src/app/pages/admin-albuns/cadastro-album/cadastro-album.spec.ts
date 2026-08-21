import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { CadastroAlbum } from './cadastro-album';
import { AlbumResponse } from '../../../models/AlbumResponse';
import { ArtistaResponse } from '../../../models/ArtistaResponse';
import { ArtistaResumo } from '../../../models/ArtistaResumoModel';

describe('CadastroAlbum', () => {
  let component: CadastroAlbum;
  let fixture: ComponentFixture<CadastroAlbum>;
  let httpMock: HttpTestingController;

  const apiArtistasUrl =
    'http://localhost:8080/api/artistas';

  const apiAlbunsUrl =
    'http://localhost:8080/api/admin/albuns';

  const artistaMock: ArtistaResponse = {
    idArtista: 1,
    nome: 'Queen',
    nomeCompleto: 'Queen',
    descricao: 'Banda britânica de rock.',
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
    idAlbum: 1,
    titulo: 'A Night at the Opera',
    anoLancamento: 1975,
    capaUrl: 'https://example.com/capa.jpg',
    artista: artistaResumoMock
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CadastroAlbum],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CadastroAlbum);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function carregarArtistas(
    artistas: ArtistaResponse[] = [artistaMock]
  ): void {
    fixture.detectChanges();

    const requisicao = httpMock.expectOne(apiArtistasUrl);

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush(artistas);
  }

  function preencherFormularioValido(): void {
    component.formulario.setValue({
      titulo: ' A Night at the Opera ',
      idArtista: artistaMock.idArtista,
      anoLancamento: 1975,
      capaUrl: ' https://example.com/capa.jpg '
    });
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('deve carregar os artistas ao inicializar', () => {
    fixture.detectChanges();

    expect(component.carregandoArtistas()).toBe(true);

    const requisicao = httpMock.expectOne(apiArtistasUrl);

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush([artistaMock]);

    expect(component.artistas).toEqual([artistaMock]);
    expect(component.carregandoArtistas()).toBe(false);
  });

  it('deve informar quando não houver artistas cadastrados', () => {
    carregarArtistas([]);

    expect(component.artistas).toEqual([]);
    expect(component.erroArtistas()).toBe(
      'Nenhum artista cadastrado. Cadastre um artista primeiro.'
    );
    expect(component.carregandoArtistas()).toBe(false);
  });

  it('deve liberar o carregamento quando a busca de artistas falhar', () => {
    fixture.detectChanges();

    httpMock
      .expectOne(apiArtistasUrl)
      .flush(
        {},
        {
          status: 500,
          statusText: 'Internal Server Error'
        }
      );

    expect(component.artistas).toEqual([]);
    expect(component.carregandoArtistas()).toBe(false);
  });

  it('não deve cadastrar quando o formulário for inválido', () => {
    carregarArtistas();

    component.salvar();

    httpMock.expectNone(apiAlbunsUrl);

    expect(component.formulario.controls.titulo.touched)
      .toBe(true);

    expect(component.formulario.controls.idArtista.touched)
      .toBe(true);

    expect(component.formulario.controls.anoLancamento.touched)
      .toBe(true);
  });

  it('deve rejeitar título apenas com espaços e URL inválida', () => {
    component.formulario.controls.titulo.setValue('   ');

    component.formulario.controls.capaUrl.setValue(
      'url-invalida'
    );

    expect(
      component.formulario.controls.titulo
        .hasError('apenasEspacos')
    ).toBe(true);

    expect(
      component.formulario.controls.capaUrl
        .hasError('urlInvalida')
    ).toBe(true);

    component.formulario.controls.capaUrl.setValue(
      'https://example.com/capa.jpg'
    );

    expect(
      component.formulario.controls.capaUrl
        .hasError('urlInvalida')
    ).toBe(false);
  });

  it('deve cadastrar o álbum e limpar o formulário', () => {
    carregarArtistas();
    preencherFormularioValido();

    component.salvar();

    expect(component.carregando()).toBe(true);

    const requisicao = httpMock.expectOne(apiAlbunsUrl);

    expect(requisicao.request.method).toBe('POST');

    expect(requisicao.request.body).toEqual({
      titulo: 'A Night at the Opera',
      idArtista: 1,
      anoLancamento: 1975,
      capaUrl: 'https://example.com/capa.jpg'
    });

    requisicao.flush(albumMock);

    expect(component.mensagemSucesso()).toBe(
      'Álbum A Night at the Opera cadastrado com sucesso!'
    );

    expect(component.mensagemErro()).toBe('');
    expect(component.carregando()).toBe(false);

    expect(component.formulario.getRawValue()).toEqual({
      titulo: '',
      idArtista: null,
      anoLancamento: null,
      capaUrl: ''
    });
  });

  it('não deve enviar outro cadastro enquanto houver requisição em andamento', () => {
    carregarArtistas();
    preencherFormularioValido();

    component.salvar();

    const requisicao = httpMock.expectOne(apiAlbunsUrl);

    component.salvar();

    httpMock.expectNone(apiAlbunsUrl);

    requisicao.flush(albumMock);

    expect(component.carregando()).toBe(false);
  });

  const cenariosDeErro = [
    {
      status: 400,
      statusText: 'Bad Request',
      corpo: {
        message: 'Título inválido.'
      },
      mensagemEsperada: 'Título inválido.'
    },
    {
      status: 400,
      statusText: 'Bad Request',
      corpo: {},
      mensagemEsperada:
        'Existem dados inválidos no formulário.'
    },
    {
      status: 401,
      statusText: 'Unauthorized',
      corpo: {},
      mensagemEsperada:
        'Sua sessão não é válida. Faça login novamente.'
    },
    {
      status: 403,
      statusText: 'Forbidden',
      corpo: {},
      mensagemEsperada:
        'Você não possui permissão para cadastrar álbuns.'
    },
    {
      status: 404,
      statusText: 'Not Found',
      corpo: {},
      mensagemEsperada:
        'O artista selecionado não foi encontrado.'
    },
    {
      status: 409,
      statusText: 'Conflict',
      corpo: {},
      mensagemEsperada:
        'Esse álbum já está cadastrado.'
    },
    {
      status: 500,
      statusText: 'Internal Server Error',
      corpo: {},
      mensagemEsperada:
        'Ocorreu um erro ao cadastrar o álbum.'
    }
  ];

  for (const cenario of cenariosDeErro) {
    it(`deve tratar resposta de erro ${cenario.status}`, () => {
      carregarArtistas();
      preencherFormularioValido();

      component.salvar();

      httpMock
        .expectOne(apiAlbunsUrl)
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

      expect(component.mensagemSucesso()).toBe('');
      expect(component.carregando()).toBe(false);
    });
  }
});
