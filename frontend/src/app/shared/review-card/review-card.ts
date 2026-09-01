import { Component, input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

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

  constructor(private readonly router: Router) {}

  protected aoFalharCapa(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;
    imagem.src = CAPA_PADRAO;
  }

  // O nome do autor fica dentro do <a> do card: evita aninhar outro
  // <a> (HTML inválido) navegando de forma programática e impedindo
  // que o clique também acione o link do card.
  protected irParaPerfilAutor(evento: Event): void {
    evento.preventDefault();
    evento.stopPropagation();

    void this.router.navigate(['/perfil', this.review().autor.id]);
  }
}
