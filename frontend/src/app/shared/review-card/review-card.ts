import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';

import type { Review } from '../../models/Review';
import { StarRating } from '../star-rating/star-rating';

const CAPA_PADRAO = '/capa-padrao.png';

@Component({
  selector: 'app-review-card',
  imports: [RouterLink, StarRating],
  templateUrl: './review-card.html',
  styleUrl: './review-card.css'
})
export class ReviewCard {

  review = input.required<Review>();

  editar = output<Review>();
  excluir = output<Review>();

  protected rotaAlvo(review: Review): string[] {
    return review.alvo.tipo === 'ALBUM'
      ? ['/albuns', String(review.alvo.id)]
      : ['/musicas', String(review.alvo.id)];
  }

  protected formatarData(data: string): string {
    return new Date(data).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  protected aoFalharCapa(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;
    imagem.src = CAPA_PADRAO;
  }

  protected aoClicarEditar(): void {
    this.editar.emit(this.review());
  }

  protected aoClicarExcluir(): void {
    this.excluir.emit(this.review());
  }
}
