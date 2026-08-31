import { Component, OnInit, computed, input, output, signal } from '@angular/core';

import type { AlbumResponse } from '../../models/AlbumResponse';
import type { MusicaListagem } from '../../models/MusicaListagem';
import type { Review, TipoAlvoReview } from '../../models/Review';
import { CatalogoService } from '../../services/catalogo';
import { ReviewService } from '../../services/review';
import { StarRating } from '../star-rating/star-rating';

interface OpcaoAlvo {
  id: number;
  rotulo: string;
}

@Component({
  selector: 'app-review-form',
  imports: [StarRating],
  templateUrl: './review-form.html',
  styleUrl: './review-form.css'
})
export class ReviewForm implements OnInit {

  reviewParaEditar = input<Review | null>(null);

  salvo = output<void>();
  cancelado = output<void>();

  protected tipoAlvo = signal<TipoAlvoReview>('MUSICA');
  protected idAlvoSelecionado = signal<number | null>(null);
  protected nota = signal(0);
  protected texto = signal('');
  protected enviando = signal(false);
  protected erro = signal<string | null>(null);

  private musicas = signal<MusicaListagem[]>([]);
  private albuns = signal<AlbumResponse[]>([]);

  protected opcoesAlvo = computed<OpcaoAlvo[]>(() =>
    this.tipoAlvo() === 'MUSICA'
      ? this.musicas().map(musica => ({
          id: musica.id,
          rotulo: `${musica.titulo} — ${musica.artistaPrincipal.nome}`
        }))
      : this.albuns().map(album => ({
          id: album.idAlbum,
          rotulo: `${album.titulo} — ${album.artista.nome}`
        }))
  );

  protected estaEditando = computed(() => this.reviewParaEditar() !== null);

  protected podeSalvar = computed(() => {
    if (this.nota() < 1 || this.nota() > 5) {
      return false;
    }

    return this.estaEditando() || this.idAlvoSelecionado() !== null;
  });

  constructor(
    private readonly catalogoService: CatalogoService,
    private readonly reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    const review = this.reviewParaEditar();

    if (review) {
      this.nota.set(review.nota);
      this.texto.set(review.texto ?? '');
      return;
    }

    this.catalogoService.listarMusicas()
      .subscribe(musicas => this.musicas.set(musicas));

    this.catalogoService.listarAlbuns()
      .subscribe(albuns => this.albuns.set(albuns));
  }

  protected selecionarTipo(tipo: TipoAlvoReview): void {
    this.tipoAlvo.set(tipo);
    this.idAlvoSelecionado.set(null);
  }

  protected aoSelecionarAlvo(evento: Event): void {
    const valor = (evento.target as HTMLSelectElement).value;
    this.idAlvoSelecionado.set(valor ? Number(valor) : null);
  }

  protected aoDigitarTexto(evento: Event): void {
    this.texto.set((evento.target as HTMLTextAreaElement).value);
  }

  protected salvar(evento: Event): void {
    evento.preventDefault();

    if (!this.podeSalvar() || this.enviando()) {
      return;
    }

    this.enviando.set(true);
    this.erro.set(null);

    const textoNormalizado = this.texto().trim() || null;
    const review = this.reviewParaEditar();

    const operacao = review
      ? this.reviewService.atualizar(review.idReview, {
          nota: this.nota(),
          texto: textoNormalizado
        })
      : this.reviewService.criar({
          idMusica: this.tipoAlvo() === 'MUSICA'
            ? this.idAlvoSelecionado()
            : null,
          idAlbum: this.tipoAlvo() === 'ALBUM'
            ? this.idAlvoSelecionado()
            : null,
          nota: this.nota(),
          texto: textoNormalizado
        });

    operacao.subscribe({
      next: () => {
        this.enviando.set(false);
        this.salvo.emit();
      },
      error: (erro: Error) => {
        this.enviando.set(false);
        this.erro.set(erro.message);
      }
    });
  }

  protected cancelar(): void {
    this.cancelado.emit();
  }
}
