import {
  Component,
  input,
  output
} from '@angular/core';

import type { MusicaListagem } from '../../../models/MusicaListagem';

@Component({
  selector: 'app-acoes-musica',
  templateUrl: './acoes-musica.html',
  styleUrl: './acoes-musica.css'
})
export class AcoesMusica {

  readonly musica = input.required<MusicaListagem>();
  readonly bloqueado = input(false);
  readonly excluindo = input(false);

  readonly editar = output<number>();
  readonly excluir = output<MusicaListagem>();
}
