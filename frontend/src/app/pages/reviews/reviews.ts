import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import type { PaginaResponse } from '../../models/PaginaResponse';
import type { Review } from '../../models/Review';
import { ReviewService } from '../../services/review';
import { ReviewCard } from '../../shared/review-card/review-card';
import { ReviewForm } from '../../shared/review-form/review-form';

type Aba = 'FEED' | 'MINHAS';

@Component({
  selector: 'app-reviews',
  imports: [RouterLink, ReviewCard, ReviewForm],
  templateUrl: './reviews.html',
  styleUrl: './reviews.css'
})
export class Reviews implements OnInit {

  protected abaAtiva = signal<Aba>('FEED');
  protected pagina = signal<PaginaResponse<Review> | null>(null);
  protected carregando = signal(false);
  protected erro = signal<string | null>(null);

  protected criandoReview = signal(false);
  protected reviewEmEdicao = signal<Review | null>(null);

  constructor(private readonly reviewService: ReviewService) {}

  ngOnInit(): void {
    this.carregarPagina(0);
  }

  protected trocarAba(aba: Aba): void {
    if (this.abaAtiva() === aba) {
      return;
    }

    this.abaAtiva.set(aba);
    this.fecharFormularios();
    this.carregarPagina(0);
  }

  protected carregarPagina(numeroPagina: number): void {
    this.carregando.set(true);
    this.erro.set(null);

    const requisicao = this.abaAtiva() === 'FEED'
      ? this.reviewService.listarFeed(numeroPagina)
      : this.reviewService.listarMinhas(numeroPagina);

    requisicao.subscribe({
      next: pagina => {
        this.pagina.set(pagina);
        this.carregando.set(false);
      },
      error: (erro: Error) => {
        this.erro.set(erro.message);
        this.carregando.set(false);
      }
    });
  }

  protected paginaAnterior(): void {
    const atual = this.pagina();

    if (atual && atual.paginaAtual > 0) {
      this.carregarPagina(atual.paginaAtual - 1);
    }
  }

  protected proximaPagina(): void {
    const atual = this.pagina();

    if (atual && atual.paginaAtual + 1 < atual.totalPaginas) {
      this.carregarPagina(atual.paginaAtual + 1);
    }
  }

  protected abrirNovaReview(): void {
    this.reviewEmEdicao.set(null);
    this.criandoReview.set(true);
  }

  protected editarReview(review: Review): void {
    this.criandoReview.set(false);
    this.reviewEmEdicao.set(review);
  }

  protected excluirReview(review: Review): void {
    const confirmou = confirm(
      `Excluir sua avaliação de "${review.alvo.titulo}"?`
    );

    if (!confirmou) {
      return;
    }

    this.reviewService.excluir(review.idReview).subscribe({
      next: () => this.carregarPagina(this.pagina()?.paginaAtual ?? 0),
      error: (erro: Error) => this.erro.set(erro.message)
    });
  }

  protected aoSalvarFormulario(): void {
    this.fecharFormularios();
    this.carregarPagina(0);
  }

  protected fecharFormularios(): void {
    this.criandoReview.set(false);
    this.reviewEmEdicao.set(null);
  }
}
