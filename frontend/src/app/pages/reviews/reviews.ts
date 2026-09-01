import { Component, OnInit, computed, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import type { Review } from '../../models/Review';
import { ReviewService } from '../../services/review';
import { ReviewCard } from '../../shared/review-card/review-card';

type Escopo = 'TODAS' | 'MINHAS';

@Component({
  selector: 'app-reviews',
  imports: [RouterLink, ReviewCard],
  templateUrl: './reviews.html',
  styleUrl: './reviews.css'
})
export class Reviews implements OnInit {

  protected readonly escopo = signal<Escopo>('TODAS');
  protected readonly reviews = signal<Review[]>([]);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly reviewsAlbuns = computed(
    () => this.reviews().filter(review => review.alvo.tipo === 'ALBUM')
  );

  protected readonly reviewsMusicas = computed(
    () => this.reviews().filter(review => review.alvo.tipo === 'MUSICA')
  );

  constructor(
    private readonly reviewService: ReviewService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Permite abrir direto na aba "Minhas reviews" via /reviews?escopo=MINHAS
    // (usado pelo atalho da biblioteca na sidebar da home).
    if (this.route.snapshot.queryParamMap.get('escopo') === 'MINHAS') {
      this.escopo.set('MINHAS');
    }

    this.carregar();
  }

  protected trocarEscopo(escopo: Escopo): void {
    if (this.escopo() === escopo) {
      return;
    }

    this.escopo.set(escopo);
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);

    const requisicao = this.escopo() === 'TODAS'
      ? this.reviewService.listarFeed()
      : this.reviewService.listarMinhas();

    requisicao.subscribe({
      next: pagina => {
        this.reviews.set(pagina.itens);
        this.carregando.set(false);
      },
      error: (erro: Error) => {
        this.erro.set(erro.message);
        this.carregando.set(false);
      }
    });
  }
}
