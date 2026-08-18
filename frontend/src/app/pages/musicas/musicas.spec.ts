import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';

import { Musicas } from './musicas';

describe('Musicas', () => {
  let component: Musicas;
  let fixture: ComponentFixture<Musicas>;
  let httpMock: HttpTestingController;
  let router: Router;

  const musicasUrl = 'http://localhost:8080/api/musicas';
  const artistasUrl = 'http://localhost:8080/api/artistas';
  const albunsUrl = 'http://localhost:8080/api/albuns';
  const generosUrl = 'http://localhost:8080/api/generos';

  function paginaVazia() {
    return { itens: [], paginaAtual: 0, tamanhoPagina: 20, totalItens: 0, totalPaginas: 0 };
  }

  function configurarTestBed(queryParams: Record<string, string> = {}) {
    TestBed.configureTestingModule({
      imports: [Musicas],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { queryParamMap: convertToParamMap(queryParams) },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Musicas);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  }

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(artistasUrl).flush([]);
    httpMock.expectOne(albunsUrl).flush([]);
    httpMock.expectOne(generosUrl).flush([]);
    httpMock.expectOne(musicasUrl).flush(paginaVazia());

    expect(component).toBeTruthy();
  });

  it('deve pesquisar sem filtros ao iniciar quando a URL não tem query params', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(artistasUrl).flush([]);
    httpMock.expectOne(albunsUrl).flush([]);
    httpMock.expectOne(generosUrl).flush([]);

    const requisicaoMusicas = httpMock.expectOne(musicasUrl);
    expect(requisicaoMusicas.request.params.keys()).toHaveLength(0);

    requisicaoMusicas.flush(paginaVazia());
  });

  it('deve restaurar o filtro de título a partir da URL e pesquisar com ele', () => {
    configurarTestBed({ titulo: 'amor' });
    fixture.detectChanges();

    httpMock.expectOne(artistasUrl).flush([]);
    httpMock.expectOne(albunsUrl).flush([]);
    httpMock.expectOne(generosUrl).flush([]);

    expect(component.formulario.value.titulo).toBe('amor');

    const requisicaoMusicas = httpMock.expectOne(`${musicasUrl}?titulo=amor`);
    requisicaoMusicas.flush(paginaVazia());
  });

  it('deve exibir os resultados encontrados', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(artistasUrl).flush([]);
    httpMock.expectOne(albunsUrl).flush([]);
    httpMock.expectOne(generosUrl).flush([]);

    httpMock.expectOne(musicasUrl).flush({
      itens: [
        {
          id: 1,
          titulo: 'Bohemian Rhapsody',
          duracaoSegundos: 354,
          anoLancamento: 1975,
          artistaPrincipal: { id: 1, nome: 'Queen' },
          album: { id: 1, titulo: 'A Night at the Opera' },
          generos: [{ id: 1, nome: 'Rock' }],
        },
      ],
      paginaAtual: 0,
      tamanhoPagina: 20,
      totalItens: 1,
      totalPaginas: 1,
    });

    expect(component.musicas()).toHaveLength(1);
    expect(component.musicas()[0].titulo).toBe('Bohemian Rhapsody');
  });

  it('deve mostrar mensagem de erro quando a pesquisa falha', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(artistasUrl).flush([]);
    httpMock.expectOne(albunsUrl).flush([]);
    httpMock.expectOne(generosUrl).flush([]);

    httpMock
      .expectOne(musicasUrl)
      .flush({ message: 'erro' }, { status: 500, statusText: 'Internal Server Error' });

    expect(component.mensagemErro()).toBeTruthy();
    expect(component.musicas()).toHaveLength(0);
  });

  it('limparFiltros deve resetar o formulário e pesquisar novamente', () => {
    configurarTestBed({ titulo: 'amor' });
    fixture.detectChanges();

    httpMock.expectOne(artistasUrl).flush([]);
    httpMock.expectOne(albunsUrl).flush([]);
    httpMock.expectOne(generosUrl).flush([]);
    httpMock.expectOne(`${musicasUrl}?titulo=amor`).flush(paginaVazia());

    component.limparFiltros();

    expect(component.formulario.value.titulo).toBe('');

    httpMock.expectOne(musicasUrl).flush(paginaVazia());
  });
});
