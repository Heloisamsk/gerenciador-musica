import {
  Component,
  computed,
  input
} from '@angular/core';

import type { ArtistaResumo } from '../../../models/MusicaResponse';

@Component({
  selector: 'app-participantes-musica',
  templateUrl: './participantes-musica.html',
  styleUrl: './participantes-musica.css'
})
export class ParticipantesMusica {

  readonly participantes = input<readonly ArtistaResumo[]>([]);
  readonly descricaoAcessivel = computed(() => {
    if (this.participantes().length === 0) {
      return 'Nenhum artista participante';
    }

    return this.participantes()
      .map(artista => artista.nome)
      .join(', ');
  });
}
