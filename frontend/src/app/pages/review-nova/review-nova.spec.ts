import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  Router
} from '@angular/router';
import { vi } from 'vitest';

import type { Review } from '../../models/Review';
import { ReviewNova } from './review-nova';

describe('ReviewNova', () => {
  let fixture: ComponentFixture<ReviewNova>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api';

  async function configurar(
    queryParams: Record<string, string> = {}
  ): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ReviewNova],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap(queryParams)
            }
          }
        }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ReviewNova);
  }

  afterEach(() => {
    httpMock?.verify();
  });

  it('deve pré-preencher o alvo quando vem de query params e não buscar catálogo', async () => {
    await configurar({
      tipo: 'MUSICA',
      id: '20',
      titulo: 'Bohemian Rhapsody',
      artista: 'Queen'
    });

    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('#review-alvo')
    ).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Bohemian Rhapsody');
  });

  it('deve buscar o catálogo quando não vem alvo pré-definido', async () => {
    await configurar();
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}/musicas?size=100`).flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 100, totalItens: 0, totalPaginas: 0
    });
    httpMock.expectOne(`${apiUrl}/albuns`).flush([]);

    expect(
      fixture.nativeElement.querySelector('#review-alvo')
    ).not.toBeNull();
  });

  it('deve navegar para a página da review criada ao salvar', async () => {
    await configurar();
    const router = TestBed.inject(Router);
    const navegarSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture.detectChanges();
    httpMock.expectOne(`${apiUrl}/musicas?size=100`).flush({
      itens: [], paginaAtual: 0, tamanhoPagina: 100, totalItens: 0, totalPaginas: 0
    });
    httpMock.expectOne(`${apiUrl}/albuns`).flush([]);

    const reviewCriada = { idReview: 42 } as Review;
    fixture.componentInstance['aoSalvar'](reviewCriada);

    expect(navegarSpy).toHaveBeenCalledWith(['/reviews', 42]);
  });
});
