import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import type { PlaylistResponse } from '../../models/PlaylistResponse';

@Component({
  selector: 'app-playlist-card',
  imports: [RouterLink],
  templateUrl: './playlist-card.html',
  styleUrls: ['./playlist-card.css']
})
export class PlaylistCard {

  readonly playlist = input.required<PlaylistResponse>();

  aoFalharCapa(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;
    imagem.src = '/capa-padrao.png';
  }
}
