import { Component, computed, input, output, signal } from '@angular/core';

const ESTRELAS = [1, 2, 3, 4, 5] as const;
const PASSO = 0.5;

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

  /*
   * Fração preenchida de cada estrela (0, 0.5 ou 1), usada tanto pra
   * desenhar o preenchimento parcial quanto pro aria-label.
   */
  protected fracaoPreenchida(valor: number): number {
    const diferenca = this.notaExibida() - (valor - 1);
    return Math.min(1, Math.max(0, diferenca));
  }

  // A metade esquerda de uma estrela vale valor - 0.5, a direita vale
  // valor cheio — padrão de avaliação em meia-estrela (tipo IMDB/Yelp).
  protected aoPassarMouse(valor: number, metade: 'esquerda' | 'direita'): void {
    if (this.interativo()) {
      this.notaEmHover.set(metade === 'esquerda' ? valor - PASSO : valor);
    }
  }

  protected aoSairMouse(): void {
    this.notaEmHover.set(null);
  }

  protected aoClicar(valor: number, metade: 'esquerda' | 'direita'): void {
    if (this.interativo()) {
      this.notaEscolhida.emit(metade === 'esquerda' ? valor - PASSO : valor);
    }
  }
}
