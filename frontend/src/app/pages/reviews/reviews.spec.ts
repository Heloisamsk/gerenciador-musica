import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';

import type { PaginaResponse } from '../../models/PaginaResponse';
import type { Review } from '../../models/Review';
import { Reviews } from './reviews';

describe('Reviews', () => {
  let fixture: ComponentFixture<Reviews>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/reviews';

  const reviewDeExemplo: Review = {
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

  function paginaDeExemplo(): PaginaResponse<Review> {
    return {
      itens: [reviewDeExemplo],
      paginaAtual: 0,
      tamanhoPagina: 20,
      totalItens: 1,
      totalPaginas: 1
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

  it('deve carregar o feed ao iniciar', () => {
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}?page=0`).flush(paginaDeExemplo());
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Bohemian Rhapsody');
  });

  it('deve recarregar como "minhas reviews" ao trocar de aba', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}?page=0`).flush(paginaDeExemplo());
    fixture.detectChanges();

    fixture.componentInstance['trocarAba']('MINHAS');

    httpMock.expectOne(`${apiUrl}/minhas?page=0`).flush(paginaDeExemplo());
  });

  it('deve excluir uma review após confirmação e recarregar a lista', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}?page=0`).flush(paginaDeExemplo());
    fixture.detectChanges();

    fixture.componentInstance['excluirReview'](reviewDeExemplo);

    httpMock.expectOne(`${apiUrl}/1`).flush(null);
    httpMock.expectOne(`${apiUrl}?page=0`).flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 20, totalItens: 0, totalPaginas: 0
    });
  });

  it('não deve excluir quando o usuário cancela a confirmação', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}?page=0`).flush(paginaDeExemplo());

    fixture.componentInstance['excluirReview'](reviewDeExemplo);
  });
});
