import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import type { Review } from '../../models/Review';
import { ReviewForm } from './review-form';

describe('ReviewForm', () => {
  let fixture: ComponentFixture<ReviewForm>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReviewForm],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ReviewForm);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve carregar músicas e álbuns para escolher o alvo ao criar', () => {
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}/musicas?size=100`).flush({
      itens: [{
        id: 20,
        titulo: 'Bohemian Rhapsody',
        duracaoSegundos: 354,
        anoLancamento: 1975,
        artistaPrincipal: { idArtista: 5, nome: 'Queen' },
        album: null,
        artistasParticipantes: [],
        generos: []
      }],
      paginaAtual: 0,
      tamanhoPagina: 100,
      totalItens: 1,
      totalPaginas: 1
    });

    httpMock.expectOne(`${apiUrl}/albuns`).flush([]);
    fixture.detectChanges();

    const opcoes = fixture.nativeElement.querySelectorAll(
      '#review-alvo option'
    );

    expect(opcoes.length).toBe(2);
    expect(opcoes[1].textContent).toContain('Bohemian Rhapsody');
  });

  it('deve enviar POST ao criar uma review de música', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/musicas?size=100`).flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 100, totalItens: 0, totalPaginas: 0
    });
    httpMock.expectOne(`${apiUrl}/albuns`).flush([]);

    fixture.componentInstance['idAlvoSelecionado'].set(20);
    fixture.componentInstance['nota'].set(5);
    fixture.detectChanges();

    let salvoEmitido = false;
    fixture.componentInstance.salvo.subscribe(() => (salvoEmitido = true));

    /*
     * Dispara pelo botão de verdade (não chamando salvar() direto):
     * é o único jeito de pegar bug de (ngSubmit) sem FormsModule, que
     * faz o form navegar em vez de chamar o método.
     */
    const botaoSalvar = fixture.nativeElement.querySelector(
      '.review-form__salvar'
    ) as HTMLButtonElement;
    botaoSalvar.click();

    const requisicao = httpMock.expectOne(`${apiUrl}/reviews`);
    expect(requisicao.request.body).toEqual({
      idMusica: 20,
      idAlbum: null,
      nota: 5,
      texto: null
    });

    requisicao.flush({} as Review);

    expect(salvoEmitido).toBe(true);
  });

  it('deve pré-preencher nota e texto ao editar, sem buscar catálogo', () => {
    const reviewExistente: Review = {
      idReview: 1,
      autor: { id: 1, nome: 'Maria' },
      alvo: { tipo: 'MUSICA', id: 20, titulo: 'Bohemian Rhapsody', artista: 'Queen', capaUrl: null },
      nota: 3,
      texto: 'Legal',
      criadaEm: '2026-01-01T00:00:00Z',
      atualizadaEm: '2026-01-01T00:00:00Z',
      minhaReview: true
    };

    fixture.componentRef.setInput('reviewParaEditar', reviewExistente);
    fixture.detectChanges();

    expect(fixture.componentInstance['nota']()).toBe(3);
    expect(fixture.componentInstance['texto']()).toBe('Legal');
    expect(
      fixture.nativeElement.querySelector('#review-alvo')
    ).toBeNull();

    const botaoSalvar = fixture.nativeElement.querySelector(
      '.review-form__salvar'
    ) as HTMLButtonElement;
    botaoSalvar.click();

    httpMock.expectOne(`${apiUrl}/reviews/1`).flush(reviewExistente);
  });

  it('deve emitir cancelado ao clicar em cancelar', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/musicas?size=100`).flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 100, totalItens: 0, totalPaginas: 0
    });
    httpMock.expectOne(`${apiUrl}/albuns`).flush([]);

    let cancelado = false;
    fixture.componentInstance.cancelado.subscribe(() => (cancelado = true));

    const botao = fixture.nativeElement.querySelector(
      '.review-form__cancelar'
    ) as HTMLButtonElement;
    botao.click();

    expect(cancelado).toBe(true);
  });
});
