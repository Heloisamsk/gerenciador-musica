import {
  Component,
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
}
