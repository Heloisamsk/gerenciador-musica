import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  OnInit,
  inject,
  signal,
  viewChild
} from '@angular/core';
import { finalize } from 'rxjs';

import { AlbumRequest } from '../../../models/AlbumRequestModel';
import { ArtistaResponse } from '../../../models/ArtistaResponse';
import { AdminAlbumService } from '../../../services/admin-album.service';
import { AdminArtistaService } from '../../../services/admin-artista';
import { FormularioAlbum } from '../formulario-album/formulario-album';

interface ErroApi {
  message?: string;
}

@Component({
  selector: 'app-cadastro-album',
  imports: [FormularioAlbum],
  templateUrl: './cadastro-album.html'
})
export class CadastroAlbum implements OnInit {

  private readonly albumService = inject(AdminAlbumService);
  private readonly artistaService = inject(AdminArtistaService);
  private readonly formularioAlbum = viewChild(FormularioAlbum);

  readonly carregando = signal(false);
  readonly carregandoArtistas = signal(false);
  readonly erroArtistas = signal('');
  readonly mensagemSucesso = signal('');
  readonly mensagemErro = signal('');

  artistas: ArtistaResponse[] = [];

  ngOnInit(): void {
    this.carregarArtistas();
  }

  salvar(request: AlbumRequest): void {
    if (this.carregando()) {
      return;
    }

    this.carregando.set(true);
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    this.albumService
      .cadastrarAlbum(request)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: response => {
          this.mensagemSucesso.set(
            `Álbum ${response.titulo} cadastrado com sucesso!`
          );
          this.formularioAlbum()?.resetar();
        },
        error: (erro: HttpErrorResponse) => {
          this.mensagemErro.set(this.mensagemParaErro(erro));
        }
      });
  }

  private carregarArtistas(): void {
    this.carregandoArtistas.set(true);
    this.erroArtistas.set('');

    this.artistaService
      .listarArtistas()
      .pipe(finalize(() => this.carregandoArtistas.set(false)))
      .subscribe({
        next: artistas => {
          this.artistas = artistas;

          if (artistas.length === 0) {
            this.erroArtistas.set(
              'Nenhum artista cadastrado. Cadastre um artista primeiro.'
            );
          }
        },
        error: () => {
          this.artistas = [];
          this.erroArtistas.set(
            'Não foi possível carregar a lista de artistas.'
          );
        }
      });
  }

  private mensagemParaErro(erro: HttpErrorResponse): string {
    const mensagemApi = (erro.error as ErroApi | null)?.message;

    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor.';
      case 400:
        return mensagemApi?.trim() ||
          'Existem dados inválidos no formulário.';
      case 401:
        return 'Sua sessão não é válida. Faça login novamente.';
      case 403:
        return 'Você não possui permissão para cadastrar álbuns.';
      case 404:
        return 'O artista selecionado não foi encontrado.';
      case 409:
        return 'Esse álbum já está cadastrado.';
      default:
        return 'Ocorreu um erro ao cadastrar o álbum.';
    }
  }
}
