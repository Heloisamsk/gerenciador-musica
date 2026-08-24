import {
  Component,
  computed,
  inject,
  input
} from '@angular/core';
import {
  DomSanitizer,
  SafeResourceUrl
} from '@angular/platform-browser';

import { criarLinkEmbedYoutube } from '../youtube-video';

@Component({
  selector: 'app-youtube-player',
  templateUrl: './youtube-player.html',
  styleUrl: './youtube-player.css'
})
export class YoutubePlayer {

  private readonly sanitizer = inject(DomSanitizer);

  readonly videoId = input.required<string>();
  readonly titulo = input.required<string>();

  readonly urlSegura = computed<SafeResourceUrl | null>(() => {
    const url = criarLinkEmbedYoutube(this.videoId());

    return url === null
      ? null
      : this.sanitizer.bypassSecurityTrustResourceUrl(url);
  });
}
