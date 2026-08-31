import { Component, OnInit, signal } from '@angular/core';
import { catchError, of } from 'rxjs';

import type { AlbumCapaPublica } from '../../models/AlbumCapaPublica';
import { CatalogoService } from '../../services/catalogo';

type CapaFlutuante = AlbumCapaPublica & {
  top: number;
  left: number;
  tamanho: number;
  duracao: number;
  atraso: number;
  opacidade: number;
  deslocamentoX: string;
  deslocamentoY: string;
};

const QUANTIDADE_MAXIMA = 16;
const CAPA_PADRAO = '/capa-padrao.png';

@Component({
  selector: 'app-album-backdrop',
  templateUrl: './album-backdrop.html',
  styleUrl: './album-backdrop.css'
})
export class AlbumBackdrop implements OnInit {

  capas = signal<CapaFlutuante[]>([]);

  constructor(
    private readonly catalogoService: CatalogoService
  ) {}

  ngOnInit(): void {
    this.catalogoService.listarCapasPublicas()
      .pipe(catchError(() => of([])))
      .subscribe(capas => this.capas.set(this.prepararCapas(capas)));
  }

  aoFalharImagem(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;
    imagem.src = CAPA_PADRAO;
  }

  private prepararCapas(
    capas: AlbumCapaPublica[]
  ): CapaFlutuante[] {
    return this.embaralhar(capas)
      .slice(0, QUANTIDADE_MAXIMA)
      .map(capa => ({
        ...capa,
        top: this.numeroEntre(-6, 92),
        left: this.numeroEntre(-6, 92),
        tamanho: this.numeroEntre(64, 148),
        duracao: this.numeroEntre(38, 78),
        atraso: this.numeroEntre(-40, 0),
        opacidade: this.numeroEntre(22, 42) / 100,
        deslocamentoX: `${this.numeroEntre(-28, 28)}px`,
        deslocamentoY: `${this.numeroEntre(-34, 34)}px`
      }));
  }

  private embaralhar<T>(itens: readonly T[]): T[] {
    const copia = [...itens];

    for (let indice = copia.length - 1; indice > 0; indice--) {
      const sorteado = Math.floor(this.aleatorio() * (indice + 1));

      [copia[indice], copia[sorteado]] =
        [copia[sorteado], copia[indice]];
    }

    return copia;
  }

  private numeroEntre(minimo: number, maximo: number): number {
    return this.aleatorio() * (maximo - minimo) + minimo;
  }

  /*
   * O SonarCloud sinaliza Math.random() como sensível a segurança
   * (previsível demais para uso criptográfico). Aqui o uso é só
   * decorativo (embaralhar capas no fundo), mas trocar por
   * crypto.getRandomValues() remove o alerta sem precisar justificar
   * manualmente cada ocorrência no painel do Sonar.
   */
  private aleatorio(): number {
    const valores = new Uint32Array(1);
    crypto.getRandomValues(valores);
    return valores[0] / 0x100000000;
  }
}
