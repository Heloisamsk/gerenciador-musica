import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';

import type { Review } from '../../models/Review';
import { Reviews } from './reviews';

describe('Reviews', () => {
  let fixture: ComponentFixture<Reviews>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/reviews';

  function reviewMusica(): Review {
    return {
      idReview: 1,
      autor: { id: 1, nome: 'Maria' },
      alvo: {
        tipo: 'MUSICA',
        id: 20,
        titulo: 'Bohemian Rhapsody',
        artista: 'Queen',
        capaUrl: null
      },
      nota: 5,
      texto: 'Obra-prima',
      criadaEm: '2026-01-10T12:00:00Z',
      atualizadaEm: '2026-01-10T12:00:00Z',
      minhaReview: true
    };
  }

  function reviewAlbum(): Review {
    return {
      idReview: 2,
      autor: { id: 1, nome: 'Maria' },
      alvo: {
        tipo: 'ALBUM',
        id: 10,
        titulo: 'A Night at the Opera',
        artista: 'Queen',
        capaUrl: null
      },
      nota: 4,
      texto: null,
      criadaEm: '2026-01-09T12:00:00Z',
      atualizadaEm: '2026-01-09T12:00:00Z',
      minhaReview: false
    };
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Reviews],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Reviews);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve separar as reviews carregadas em álbuns e músicas', () => {
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}?page=0&size=60`).flush({
      itens: [reviewMusica(), reviewAlbum()],
      paginaAtual: 0,
      tamanhoPagina: 60,
      totalItens: 2,
      totalPaginas: 1
    });
    fixture.detectChanges();

    expect(fixture.componentInstance['reviewsMusicas']()).toHaveLength(1);
    expect(fixture.componentInstance['reviewsAlbuns']()).toHaveLength(1);

    const cards = fixture.nativeElement.querySelectorAll('app-review-card');
    expect(cards.length).toBe(2);
  });

  it('deve recarregar como "minhas reviews" ao trocar de escopo', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}?page=0&size=60`).flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 60, totalItens: 0, totalPaginas: 0
    });
    fixture.detectChanges();

    fixture.componentInstance['trocarEscopo']('MINHAS');

    httpMock.expectOne(`${apiUrl}/minhas?page=0&size=60`).flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 60, totalItens: 0, totalPaginas: 0
    });

    expect(fixture.componentInstance['escopo']()).toBe('MINHAS');
  });

  it('deve abrir direto em "Minhas reviews" quando a URL tiver ?escopo=MINHAS', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [Reviews],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap({ escopo: 'MINHAS' })
            }
          }
        }
      ]
    });

    const httpMockLocal = TestBed.inject(HttpTestingController);
    const fixtureLocal = TestBed.createComponent(Reviews);
    fixtureLocal.detectChanges();

    expect(fixtureLocal.componentInstance['escopo']()).toBe('MINHAS');

    httpMockLocal.expectOne(`${apiUrl}/minhas?page=0&size=60`).flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 60, totalItens: 0, totalPaginas: 0
    });

    httpMockLocal.verify();
  });

  it('deve exibir mensagem de estado vazio por seção', () => {
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}?page=0&size=60`).flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 60, totalItens: 0, totalPaginas: 0
    });
    fixture.detectChanges();

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Ainda não há reviews de álbuns.');
    expect(texto).toContain('Ainda não há reviews de músicas.');
  });
});
