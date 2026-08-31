import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import type { Review } from '../../models/Review';
import { ReviewCard } from './review-card';

describe('ReviewCard', () => {
  let fixture: ComponentFixture<ReviewCard>;

  const reviewDeExemplo: Review = {
    idReview: 1,
    autor: { id: 1, nome: 'Maria' },
    alvo: {
      tipo: 'MUSICA',
      id: 20,
      titulo: 'Bohemian Rhapsody',
      artista: 'Queen',
      capaUrl: '/capa.jpg'
    },
    nota: 5,
    texto: 'Obra-prima',
    criadaEm: '2026-01-10T12:00:00Z',
    atualizadaEm: '2026-01-10T12:00:00Z',
    minhaReview: true
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReviewCard],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(ReviewCard);
    fixture.componentRef.setInput('review', reviewDeExemplo);
    fixture.detectChanges();
  });

  it('deve exibir o alvo, a nota e o texto da review', () => {
    const texto = fixture.nativeElement.textContent;

    expect(texto).toContain('Bohemian Rhapsody');
    expect(texto).toContain('Queen');
    expect(texto).toContain('Obra-prima');
  });

  it('deve mostrar ações de editar/excluir somente quando é a própria review', () => {
    expect(
      fixture.nativeElement.querySelector('.review-card__acoes')
    ).not.toBeNull();

    fixture.componentRef.setInput('review', {
      ...reviewDeExemplo,
      minhaReview: false
    });
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.review-card__acoes')
    ).toBeNull();
  });

  it('deve emitir o evento excluir com a review atual', () => {
    let reviewExcluida: Review | undefined;
    fixture.componentInstance.excluir.subscribe(
      (review: Review) => (reviewExcluida = review)
    );

    const botaoExcluir = fixture.nativeElement.querySelector(
      '.review-card__acao--excluir'
    ) as HTMLButtonElement;
    botaoExcluir.click();

    expect(reviewExcluida?.idReview).toBe(1);
  });
});
