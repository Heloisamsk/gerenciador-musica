import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AlbumResponse } from '../../models/AlbumResponse';
import { AdminAlbumService } from '../../services/admin-album.service';

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
  readonly mensagemErro = signal('');
  readonly mensagemSucesso = signal('');

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

  usarCapaAlternativa(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;

    if (!imagem.src.endsWith(this.capaAlternativa)) {
      imagem.src = this.capaAlternativa;
    }
  }

  private recuperarMensagemDaEdicao(): void {
    const mensagem = this.router
      .getCurrentNavigation()
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
}
