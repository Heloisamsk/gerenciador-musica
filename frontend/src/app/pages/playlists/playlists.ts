import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { PlaylistResponse } from '../../models/PlaylistResponse';
import { PlaylistService } from '../../services/playlist';

@Component({
  selector: 'app-playlists',
  imports: [RouterLink],
  templateUrl: './playlists.html',
  styleUrls: ['./playlists.css']
})
export class Playlists implements OnInit {

  playlists: PlaylistResponse[] = [];
  carregando = false;
  mensagemErro = '';

  constructor(
    private playlistService: PlaylistService,
    private changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.carregarPlaylists();
  }

  carregarPlaylists(): void {
    this.carregando = true;
    this.mensagemErro = '';

    this.playlistService.listarMinhas()
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: (dados) => {
          this.playlists = dados;
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);

          if (erro.status === 401) {
            this.mensagemErro = 'Sua sessão expirou. Faça login novamente.';
          } else if (erro.status === 403) {
            this.mensagemErro = 'Você não tem permissão para ver essas playlists.';
          } else {
            this.mensagemErro = 'Não foi possível carregar suas playlists. Tente novamente mais tarde.';
          }
        }
      });
  }
}