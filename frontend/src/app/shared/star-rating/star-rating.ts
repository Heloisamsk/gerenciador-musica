import { Component, computed, input, output, signal } from '@angular/core';

const ESTRELAS = [1, 2, 3, 4, 5] as const;

let proximoId = 0;

@Component({
  selector: 'app-star-rating',
  templateUrl: './star-rating.html',
  styleUrl: './star-rating.css'
})
export class StarRating {

  protected readonly grupoNome = `star-rating-${proximoId++}`;

  readonly nota = input<number>(0);
  readonly interativo = input(false);
  readonly rotulo = input('Nota');

  readonly notaEscolhida = output<number>();

  protected readonly estrelas = ESTRELAS;

  private readonly notaEmHover = signal<number | null>(null);

  protected readonly notaExibida = computed(
    () => this.notaEmHover() ?? this.nota()
  );

  protected aoPassarMouse(valor: number): void {
    if (this.interativo()) {
      this.notaEmHover.set(valor);
    }
  }

  protected aoSairMouse(): void {
    this.notaEmHover.set(null);
  }

  protected aoClicar(valor: number): void {
    if (this.interativo()) {
      this.notaEscolhida.emit(valor);
    }
  }
}
