import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { PlaylistResponse } from '../../models/PlaylistResponse';
import { PlaylistService } from '../../services/playlist';

@Component({
  selector: 'app-playlist-detalhe',
  imports: [RouterLink],
  templateUrl: './playlist-detalhe.html',
  styleUrls: ['./playlist-detalhe.css']
})
export class PlaylistDetalhe implements OnInit {

  playlist: PlaylistResponse | null = null;
  carregando = false;
  mensagemErro = '';

  constructor(
    private route: ActivatedRoute,
    private playlistService: PlaylistService
  ) {}

  ngOnInit(): void {
    this.carregarPlaylist();
  }

  carregarPlaylist(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      this.mensagemErro = 'Playlist inválida.';
      return;
    }

    this.carregando = true;
    this.mensagemErro = '';

    this.playlistService.buscarPorId(Number(idParam))
      .pipe(
        finalize(() => this.carregando = false)
      )
      .subscribe({
        next: (dados) => {
          this.playlist = dados;
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);

          if (erro.status === 401) {
            this.mensagemErro = 'Sua sessão expirou. Faça login novamente.';
          } else if (erro.status === 403) {
            this.mensagemErro = 'Você não tem permissão para ver essa playlist.';
          } else if (erro.status === 404) {
            this.mensagemErro = 'Essa playlist não existe ou foi removida.';
          } else {
            this.mensagemErro = 'Não foi possível carregar a playlist. Tente novamente mais tarde.';
          }
        }
      });
  }
}