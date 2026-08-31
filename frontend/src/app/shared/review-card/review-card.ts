import { Component, input } from '@angular/core';
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

  readonly review = input.required<Review>();

  protected aoFalharCapa(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;
    imagem.src = CAPA_PADRAO;
  }
}
