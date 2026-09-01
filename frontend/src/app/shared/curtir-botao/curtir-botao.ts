import { Component, OnInit, input, output, signal } from '@angular/core';
import type { Observable } from 'rxjs';

import { CurtidaService } from '../../services/curtida';

@Component({
  selector: 'app-curtir-botao',
  templateUrl: './curtir-botao.html',
  styleUrl: './curtir-botao.css'
})
export class CurtirBotao implements OnInit {

  readonly tipo = input.required<'musica' | 'album'>();
  readonly id = input.required<number>();
  readonly curtidoInicial = input.required<boolean>();

  readonly curtidoChange = output<boolean>();

  protected readonly curtido = signal(false);
  protected readonly carregando = signal(false);

  constructor(
    private readonly curtidaService: CurtidaService
  ) {}

  ngOnInit(): void {
    this.curtido.set(this.curtidoInicial());
  }

  alternar(evento: Event): void {
    evento.preventDefault();
    evento.stopPropagation();

    if (this.carregando()) {
      return;
    }

    const novoValor = !this.curtido();

    this.curtido.set(novoValor);
    this.carregando.set(true);

    this.obterChamada(novoValor).subscribe({
      next: () => {
        this.carregando.set(false);
        this.curtidoChange.emit(novoValor);
      },
      error: () => {
        this.curtido.set(!novoValor);
        this.carregando.set(false);
      }
    });
  }

  private obterChamada(curtir: boolean): Observable<void> {
    if (this.tipo() === 'musica') {
      return curtir
        ? this.curtidaService.curtirMusica(this.id())
        : this.curtidaService.descurtirMusica(this.id());
    }

    return curtir
      ? this.curtidaService.curtirAlbum(this.id())
      : this.curtidaService.descurtirAlbum(this.id());
  }
}
