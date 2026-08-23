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

import { ArtistaResponse } from '../../models/ArtistaResponse';
import { AdminArtistaService } from '../../services/admin-artista';

interface ErroApi {
  message?: string;
}

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
  readonly excluindoId = signal<number | null>(null);
  readonly mensagemErro = signal('');
  readonly mensagemErroExclusao = signal('');
  readonly mensagemSucesso = signal('');
  readonly operacaoEmAndamento = computed(
    () => this.carregando() || this.excluindoId() !== null
  );

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

  editarArtista(idArtista: number): void {
    if (this.operacaoEmAndamento()) {
      return;
    }

    void this.router.navigate([
      '/admin/banco/artistas',
      idArtista,
      'editar'
    ]);
  }

  excluirArtista(artista: ArtistaResponse): void {
    if (this.operacaoEmAndamento()) {
      return;
    }

    const confirmou = window.confirm(
      `Tem certeza que deseja excluir o artista "${artista.nome}"?`
    );

    if (!confirmou) {
      return;
    }

    this.excluindoId.set(artista.idArtista);
    this.mensagemSucesso.set('');
    this.mensagemErroExclusao.set('');

    this.artistaService
      .excluir(artista.idArtista)
      .pipe(finalize(() => this.excluindoId.set(null)))
      .subscribe({
        complete: () => {
          this.mensagemSucesso.set(
            `Artista ${artista.nome} excluído com sucesso!`
          );
          this.carregarArtistas();
        },
        error: (erro: HttpErrorResponse) => {
          this.mensagemErroExclusao.set(
            this.mensagemParaErroDeExclusao(erro)
          );
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
        return 'Você não possui permissão para visualizar os artistas.';
      case 500:
        return 'Ocorreu um erro no servidor. Tente novamente mais tarde.';
      default:
        return 'Não foi possível carregar os artistas.';
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
        return 'Você não possui permissão para excluir artistas.';
      case 404:
        return 'O artista não foi encontrado. Atualize a listagem.';
      case 409:
        return this.mensagemDeConflito(erro);
      case 500:
        return 'Ocorreu um erro no servidor ao excluir o artista.';
      default:
        return 'Não foi possível excluir o artista.';
    }
  }

  private mensagemDeConflito(erro: HttpErrorResponse): string {
    const mensagemApi = (erro.error as ErroApi | null)?.message;

    return mensagemApi?.trim() ||
      'Não é possível excluir o artista porque ele possui ' +
      'músicas ou álbuns associados.';
  }
}
