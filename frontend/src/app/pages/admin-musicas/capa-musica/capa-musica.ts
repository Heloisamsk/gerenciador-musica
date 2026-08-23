import {
  Component,
  computed,
  input,
  signal
} from '@angular/core';

import type { MusicaListagem } from '../../../models/MusicaListagem';

@Component({
  selector: 'app-capa-musica',
  templateUrl: './capa-musica.html',
  styleUrl: './capa-musica.css'
})
export class CapaMusica {

  readonly musica = input.required<MusicaListagem>();
  readonly erroAoCarregar = signal(false);
  readonly capaDisponivel = computed(
    () => Boolean(this.musica().album?.capaUrl) && !this.erroAoCarregar()
  );

  registrarErro(): void {
    this.erroAoCarregar.set(true);
  }
}
