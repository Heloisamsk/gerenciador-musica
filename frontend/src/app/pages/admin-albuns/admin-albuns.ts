import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AlbumResponse } from '../../models/AlbumResponse';
import { AdminAlbumService } from '../../services/admin-album.service';

interface ErroApi {
  message?: string;
}

@Component({
  selector: 'app-admin-albuns',
  imports: [RouterLink],
  templateUrl: './admin-albuns.html',
  styleUrl: './admin-albuns.css'
})
export class AdminAlbuns implements OnInit {

  private readonly albumService = inject(AdminAlbumService);
  private readonly router = inject(Router);

  readonly capaAlternativa = '/capa-padrao.png';
  readonly albuns = signal<AlbumResponse[]>([]);
  readonly carregando = signal(false);
  readonly excluindoId = signal<number | null>(null);
  readonly mensagemErro = signal('');
  readonly mensagemErroExclusao = signal('');
  readonly mensagemSucesso = signal('');
  readonly operacaoEmAndamento = computed(
    () => this.carregando() || this.excluindoId() !== null
  );

  ngOnInit(): void {
    this.recuperarMensagemDaEdicao();
    this.carregarAlbuns();
  }

  carregarAlbuns(): void {
    if (this.carregando()) {
      return;
    }

    this.carregando.set(true);
    this.mensagemErro.set('');

    this.albumService
      .listarAlbuns()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: albuns => this.albuns.set(albuns),
        error: (erro: HttpErrorResponse) => {
          this.albuns.set([]);
          this.mensagemErro.set(this.mensagemParaErro(erro));
        }
      });
  }

  editarAlbum(idAlbum: number): void {
    if (this.operacaoEmAndamento()) {
      return;
    }

    void this.router.navigate([
      '/admin/banco/albuns',
      idAlbum,
      'editar'
    ]);
  }

  excluirAlbum(album: AlbumResponse): void {
    if (this.operacaoEmAndamento()) {
      return;
    }

    const confirmou = window.confirm(
      `Tem certeza que deseja excluir o álbum "${album.titulo}", ` +
      `de ${album.artista.nome}?`
    );

    if (!confirmou) {
      return;
    }

    this.excluindoId.set(album.idAlbum);
    this.mensagemSucesso.set('');
    this.mensagemErroExclusao.set('');

    this.albumService
      .excluirAlbum(album.idAlbum)
      .pipe(finalize(() => this.excluindoId.set(null)))
      .subscribe({
        complete: () => {
          this.mensagemSucesso.set(
            `Álbum ${album.titulo} excluído com sucesso!`
          );
          this.carregarAlbuns();
        },
        error: (erro: HttpErrorResponse) => {
          this.mensagemErroExclusao.set(
            this.mensagemParaErroDeExclusao(erro)
          );
        }
      });
  }

  usarCapaAlternativa(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;

    if (!imagem.src.endsWith(this.capaAlternativa)) {
      imagem.src = this.capaAlternativa;
    }
  }

  private recuperarMensagemDaEdicao(): void {
    const mensagem = this.router
      .currentNavigation()
      ?.extras
      .state?.['mensagemSucesso'];

    if (typeof mensagem === 'string') {
      this.mensagemSucesso.set(mensagem);
    }
  }

  private mensagemParaErro(erro: HttpErrorResponse): string {
    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor.';
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não possui permissão para visualizar os álbuns.';
      case 500:
        return 'Ocorreu um erro no servidor. Tente novamente mais tarde.';
      default:
        return 'Não foi possível carregar os álbuns.';
    }
  }

  private mensagemParaErroDeExclusao(
    erro: HttpErrorResponse
  ): string {
    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor.';
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não possui permissão para excluir álbuns.';
      case 404:
        return 'O álbum não foi encontrado. Atualize a listagem.';
      case 409:
        return this.mensagemDeConflito(erro);
      case 500:
        return 'Ocorreu um erro no servidor ao excluir o álbum.';
      default:
        return 'Não foi possível excluir o álbum.';
    }
  }

  private mensagemDeConflito(erro: HttpErrorResponse): string {
    const mensagemApi = (erro.error as ErroApi | null)?.message;

    return mensagemApi?.trim() ||
      'Não é possível excluir o álbum porque ele possui ' +
      'músicas associadas.';
  }
}
