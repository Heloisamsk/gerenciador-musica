import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import type { Review } from '../models/Review';
import { ReviewService } from './review';

describe('ReviewService', () => {
  let service: ReviewService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/reviews';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ReviewService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ReviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function reviewDeExemplo(): Review {
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

  it('deve enviar POST para criar uma review', () => {
    let resultado: Review | undefined;

    service.criar({
      idMusica: 20,
      idAlbum: null,
      nota: 5,
      texto: 'Obra-prima'
    }).subscribe(review => resultado = review);

    const requisicao = httpMock.expectOne(apiUrl);
    expect(requisicao.request.method).toBe('POST');
    requisicao.flush(reviewDeExemplo());

    expect(resultado?.idReview).toBe(1);
  });

  it('deve enviar PUT para atualizar uma review', () => {
    service.atualizar(1, { nota: 4, texto: null }).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/1`);
    expect(requisicao.request.method).toBe('PUT');
    requisicao.flush(reviewDeExemplo());
  });

  it('deve enviar DELETE para excluir uma review', () => {
    service.excluir(1).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/1`);
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null);
  });

  it('deve buscar uma review por id', () => {
    let resultado: Review | undefined;

    service.buscarPorId(1).subscribe(review => resultado = review);

    const requisicao = httpMock.expectOne(`${apiUrl}/1`);
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush(reviewDeExemplo());

    expect(resultado?.alvo.titulo).toBe('Bohemian Rhapsody');
  });

  it('deve listar o feed com paginação e tamanho customizados', () => {
    service.listarFeed(2, 10).subscribe();

    const requisicao = httpMock.expectOne(
      `${apiUrl}?page=2&size=10`
    );
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush({
      itens: [], paginaAtual: 2, tamanhoPagina: 10, totalItens: 0, totalPaginas: 0
    });
  });

  it('deve listar minhas reviews', () => {
    service.listarMinhas().subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/minhas?page=0&size=60`);
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 60, totalItens: 0, totalPaginas: 0
    });
  });

  it('deve listar reviews de uma música', () => {
    service.listarPorMusica(20).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/musicas/20?page=0`);
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 20, totalItens: 0, totalPaginas: 0
    });
  });

  it('deve listar reviews de um álbum', () => {
    service.listarPorAlbum(10).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/albuns/10?page=0`);
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 20, totalItens: 0, totalPaginas: 0
    });
  });

  it('deve propagar uma mensagem amigável quando a requisição falha', () => {
    let erro: Error | undefined;

    service.excluir(999).subscribe({
      error: (e: Error) => erro = e
    });

    httpMock.expectOne(`${apiUrl}/999`).flush(
      { message: 'Review não encontrada com o ID: 999' },
      { status: 404, statusText: 'Not Found' }
    );

    expect(erro?.message).toBe('Review não encontrada com o ID: 999');
  });
});
