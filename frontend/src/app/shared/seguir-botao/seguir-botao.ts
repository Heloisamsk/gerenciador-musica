import { Component, OnInit, input, output, signal } from '@angular/core';
import type { Observable } from 'rxjs';

import { SeguidorService } from '../../services/seguidor';

@Component({
  selector: 'app-seguir-botao',
  templateUrl: './seguir-botao.html',
  styleUrl: './seguir-botao.css'
})
export class SeguirBotao implements OnInit {

  readonly id = input.required<number>();
  readonly seguindoInicial = input.required<boolean>();
  readonly tipo = input<'artista' | 'usuario'>('artista');

  readonly seguindoChange = output<boolean>();

  protected readonly seguindo = signal(false);
  protected readonly carregando = signal(false);

  constructor(
    private readonly seguidorService: SeguidorService
  ) {}

  ngOnInit(): void {
    this.seguindo.set(this.seguindoInicial());
  }

  alternar(): void {
    if (this.carregando()) {
      return;
    }

    const novoValor = !this.seguindo();

    this.seguindo.set(novoValor);
    this.carregando.set(true);

    this.obterChamada(novoValor).subscribe({
      next: () => {
        this.carregando.set(false);
        this.seguindoChange.emit(novoValor);
      },
      error: () => {
        this.seguindo.set(!novoValor);
        this.carregando.set(false);
      }
    });
  }

  private obterChamada(seguir: boolean): Observable<void> {
    if (this.tipo() === 'usuario') {
      return seguir
        ? this.seguidorService.seguirUsuario(this.id())
        : this.seguidorService.deixarDeSeguirUsuario(this.id());
    }

    return seguir
      ? this.seguidorService.seguirArtista(this.id())
      : this.seguidorService.deixarDeSeguirArtista(this.id());
  }
}
