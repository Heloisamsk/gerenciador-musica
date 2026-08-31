import { Component, OnInit, signal } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import type { Review, TipoAlvoReview } from '../../models/Review';
import { AlvoFixo, ReviewForm } from '../../shared/review-form/review-form';

@Component({
  selector: 'app-review-nova',
  imports: [RouterLink, ReviewForm],
  templateUrl: './review-nova.html',
  styleUrl: './review-nova.css'
})
export class ReviewNova implements OnInit {

  protected readonly alvoFixo = signal<AlvoFixo | null>(null);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly location: Location
  ) {}

  ngOnInit(): void {
    const parametros = this.route.snapshot.queryParamMap;
    const tipo = parametros.get('tipo');
    const id = parametros.get('id');

    if ((tipo === 'MUSICA' || tipo === 'ALBUM') && id) {
      this.alvoFixo.set({
        tipo: tipo as TipoAlvoReview,
        id: Number(id),
        titulo: parametros.get('titulo') ?? '',
        artista: parametros.get('artista')
      });
    }
  }

  protected aoSalvar(review: Review): void {
    void this.router.navigate(['/reviews', review.idReview]);
  }

  protected aoCancelar(): void {
    this.location.back();
  }
}
