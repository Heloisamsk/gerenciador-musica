import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { PlaylistResponse } from '../../models/PlaylistResponse';
import { AlbumResponse } from '../../models/AlbumResponse';
import { PlaylistService } from '../../services/playlist';
import { CatalogoService } from '../../services/catalogo';
import { PlaylistCard } from '../../shared/playlist-card/playlist-card';
import { CurtirBotao } from '../../shared/curtir-botao/curtir-botao';

@Component({
  selector: 'app-playlists',
  imports: [RouterLink, PlaylistCard, CurtirBotao],
  templateUrl: './playlists.html',
  styleUrls: ['./playlists.css']
})
export class Playlists implements OnInit {

  playlists: PlaylistResponse[] = [];
  carregando = false;
  mensagemErro = '';

  albunsCurtidos: AlbumResponse[] = [];
  carregandoAlbuns = false;
  mensagemErroAlbuns = '';

  constructor(
    private readonly playlistService: PlaylistService,
    private readonly catalogoService: CatalogoService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.carregarPlaylists();
    this.carregarAlbunsCurtidos();
  }

  carregarAlbunsCurtidos(): void {
    this.carregandoAlbuns = true;
    this.mensagemErroAlbuns = '';

    this.catalogoService.listarAlbunsCurtidos()
      .pipe(
        finalize(() => {
          this.carregandoAlbuns = false;
          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: (dados) => {
          this.albunsCurtidos = dados;
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);
          this.mensagemErroAlbuns =
            'Não foi possível carregar os álbuns curtidos.';
        }
      });
  }

  aoDescurtirAlbum(idAlbum: number, curtido: boolean): void {
    if (curtido) {
      return;
    }

    this.albunsCurtidos = this.albunsCurtidos.filter(
      album => album.idAlbum !== idAlbum
    );
  }

  aoFalharCapaAlbum(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;
    imagem.src = '/capa-padrao.png';
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
            this.mensagemErro =
              'Sua sessão expirou. Faça login novamente.';
          } else if (erro.status === 403) {
            this.mensagemErro =
              'Você não tem permissão para ver essas playlists.';
          } else {
            this.mensagemErro =
              'Não foi possível carregar suas playlists. Tente novamente mais tarde.';
          }
        }
      });
  }
}
