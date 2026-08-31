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

  it('deve exibir o alvo, a nota e linkar para a página da review', () => {
    const texto = fixture.nativeElement.textContent;
    const link = fixture.nativeElement.querySelector('a.review-card');

    expect(texto).toContain('Bohemian Rhapsody');
    expect(texto).toContain('Queen');
    expect(link.getAttribute('href')).toBe('/reviews/1');
  });

  it('deve marcar com um selo quando é a própria review', () => {
    expect(
      fixture.nativeElement.querySelector('.review-card__selo')
    ).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Você');

    fixture.componentRef.setInput('review', {
      ...reviewDeExemplo,
      minhaReview: false
    });
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.review-card__selo')
    ).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Maria');
  });

  it('deve trocar para a capa padrão quando a imagem falhar', () => {
    const imagem = fixture.nativeElement.querySelector(
      '.review-card__capa img'
    ) as HTMLImageElement;

    imagem.dispatchEvent(new Event('error'));

    expect(imagem.src).toContain('/capa-padrao.png');
  });
});
