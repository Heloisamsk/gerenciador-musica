import {
  Component,
  computed,
  effect,
  ElementRef,
  input,
  viewChild
} from '@angular/core';

import { criarLinkEmbedYoutube } from '../youtube-video';

@Component({
  selector: 'app-youtube-player',
  templateUrl: './youtube-player.html',
  styleUrl: './youtube-player.css'
})
export class YoutubePlayer {

  private readonly playerFrame =
    viewChild<ElementRef<HTMLIFrameElement>>('playerFrame');

  readonly videoId = input.required<string>();
  readonly titulo = input.required<string>();

  readonly deveExibirPlayer = computed(
    () => criarLinkEmbedYoutube(this.videoId()) !== null
  );

  constructor() {
    effect(() => {
      const iframe = this.playerFrame()?.nativeElement;
      const url = criarLinkEmbedYoutube(this.videoId());

      if (iframe && url) {
        /*
         * O valor atribuído nunca é o link recebido do usuário. O helper
         * aceita somente IDs de 11 caracteres e fixa o domínio oficial
         * youtube-nocookie.com, impedindo a injeção de outra origem.
         */
        iframe.src = url;
      }
    });
  }
}
