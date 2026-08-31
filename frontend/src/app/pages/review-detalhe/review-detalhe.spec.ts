import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { vi } from 'vitest';

import type { Review } from '../../models/Review';
import { ReviewDetalhe } from './review-detalhe';

describe('ReviewDetalhe', () => {
  let fixture: ComponentFixture<ReviewDetalhe>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/reviews';

  function reviewDeExemplo(minhaReview = true): Review {
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
      minhaReview
    };
  }

  async function configurar(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ReviewDetalhe],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: convertToParamMap({ id: '1' }) }
          }
        }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ReviewDetalhe);
  }

  afterEach(() => {
    httpMock?.verify();
  });

  it('deve carregar e exibir a review', async () => {
    await configurar();
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}/1`).flush(reviewDeExemplo());
    fixture.detectChanges();

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Bohemian Rhapsody');
    expect(texto).toContain('Obra-prima');
  });

  it('não deve mostrar ações de editar/excluir para review de outro usuário', async () => {
    await configurar();
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}/1`).flush(reviewDeExemplo(false));
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.review-detalhe__acoes')
    ).toBeNull();
  });

  it('deve abrir o formulário de edição e atualizar a review exibida ao salvar', async () => {
    await configurar();
    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/1`).flush(reviewDeExemplo());
    fixture.detectChanges();

    const botaoEditar = fixture.nativeElement.querySelector(
      '.botao-secundario'
    ) as HTMLButtonElement;
    botaoEditar.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('app-review-form'))
      .not.toBeNull();

    fixture.componentInstance['aoSalvarEdicao']({
      ...reviewDeExemplo(),
      nota: 2
    });
    fixture.detectChanges();

    expect(fixture.componentInstance['editando']()).toBe(false);
    expect(fixture.componentInstance['review']()?.nota).toBe(2);
  });

  it('deve excluir após confirmação e navegar para /reviews', async () => {
    await configurar();
    const router = TestBed.inject(Router);
    const navegarSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/1`).flush(reviewDeExemplo());
    fixture.detectChanges();

    fixture.componentInstance['excluir']();

    httpMock.expectOne(`${apiUrl}/1`).flush(null);

    expect(navegarSpy).toHaveBeenCalledWith(['/reviews']);
  });

  it('deve exibir mensagem de erro quando a review não é encontrada', async () => {
    await configurar();
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}/1`).flush(
      { message: 'Review não encontrada com o ID: 1' },
      { status: 404, statusText: 'Not Found' }
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent)
      .toContain('Review não encontrada com o ID: 1');
  });
});
