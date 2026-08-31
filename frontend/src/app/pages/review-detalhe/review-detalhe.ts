import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import type { Review } from '../../models/Review';
import { ReviewService } from '../../services/review';
import { ReviewForm } from '../../shared/review-form/review-form';
import { StarRating } from '../../shared/star-rating/star-rating';

const CAPA_PADRAO = '/capa-padrao.png';

@Component({
  selector: 'app-review-detalhe',
  imports: [RouterLink, StarRating, ReviewForm],
  templateUrl: './review-detalhe.html',
  styleUrl: './review-detalhe.css'
})
export class ReviewDetalhe implements OnInit {

  protected readonly review = signal<Review | null>(null);
  protected readonly carregando = signal(true);
  protected readonly mensagemErro = signal('');
  protected readonly editando = signal(false);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.carregarReview(id);
  }

  protected rotaAlvo(review: Review): string[] {
    return review.alvo.tipo === 'ALBUM'
      ? ['/albuns', String(review.alvo.id)]
      : ['/musicas', String(review.alvo.id)];
  }

  protected formatarData(data: string): string {
    return new Date(data).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  protected aoFalharCapa(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;
    imagem.src = CAPA_PADRAO;
  }

  protected abrirEdicao(): void {
    this.editando.set(true);
  }

  protected aoSalvarEdicao(reviewAtualizada: Review): void {
    this.review.set(reviewAtualizada);
    this.editando.set(false);
  }

  protected cancelarEdicao(): void {
    this.editando.set(false);
  }

  protected excluir(): void {
    const review = this.review();

    if (!review) {
      return;
    }

    const confirmou = confirm(
      `Excluir sua avaliação de "${review.alvo.titulo}"?`
    );

    if (!confirmou) {
      return;
    }

    this.reviewService.excluir(review.idReview).subscribe({
      next: () => void this.router.navigate(['/reviews']),
      error: (erro: Error) => this.mensagemErro.set(erro.message)
    });
  }

  private carregarReview(id: number): void {
    this.carregando.set(true);
    this.mensagemErro.set('');

    // ReviewService já traduz o erro HTTP na própria mensagem
    // (inclui a mensagem de negócio devolvida pelo backend, como
    // "Review não encontrada com o ID: 1").
    this.reviewService.buscarPorId(id).subscribe({
      next: (review) => {
        this.review.set(review);
        this.carregando.set(false);
      },
      error: (erro: Error) => {
        this.mensagemErro.set(erro.message);
        this.carregando.set(false);
      }
    });
  }
}
