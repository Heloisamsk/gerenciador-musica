import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { ArtistaResponse } from '../../models/ArtistaResponse';
import { AdminArtistaService } from '../../services/admin-artista';

@Component({
  selector: 'app-admin-artistas',
  imports: [RouterLink],
  templateUrl: './admin-artistas.html',
  styleUrl: './admin-artistas.css'
})
export class AdminArtistas implements OnInit {

  private readonly artistaService = inject(AdminArtistaService);
  private readonly router = inject(Router);

  readonly imagemAlternativa = '/avatar-artista.png';
  readonly artistas = signal<ArtistaResponse[]>([]);
  readonly carregando = signal(false);
  readonly mensagemErro = signal('');
  readonly mensagemSucesso = signal('');

  ngOnInit(): void {
    this.recuperarMensagemDaEdicao();
    this.carregarArtistas();
  }

  carregarArtistas(): void {
    if (this.carregando()) {
      return;
    }

    this.carregando.set(true);
    this.mensagemErro.set('');

    this.artistaService
      .listarArtistas()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: artistas => this.artistas.set(artistas),
        error: (erro: HttpErrorResponse) => {
          this.artistas.set([]);
          this.mensagemErro.set(this.mensagemParaErro(erro));
        }
      });
  }

  usarImagemAlternativa(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;

    if (!imagem.src.endsWith(this.imagemAlternativa)) {
      imagem.src = this.imagemAlternativa;
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
        return 'Você não possui permissão para visualizar os artistas.';
      case 500:
        return 'Ocorreu um erro no servidor. Tente novamente mais tarde.';
      default:
        return 'Não foi possível carregar os artistas.';
    }
  }
}
