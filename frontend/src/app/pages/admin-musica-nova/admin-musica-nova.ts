import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  DestroyRef,
  inject,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import type { MusicaRequest } from '../../models/MusicaRequest';
import { AdminMusicaService } from '../../services/admin-musica';
import { FormularioMusica } from '../admin-musicas/formulario-musica/formulario-musica';

interface ErroApi {
  message?: string;
}

@Component({
  selector: 'app-admin-musica-nova',
  imports: [FormularioMusica],
  templateUrl: './admin-musica-nova.html'
})
export class AdminMusicaNova {

  private readonly musicaService = inject(AdminMusicaService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly salvando = signal(false);
  readonly mensagemSucesso = signal('');
  readonly mensagemErro = signal('');

  salvar(request: MusicaRequest): void {
    if (this.salvando()) {
      return;
    }

    this.salvando.set(true);
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    this.musicaService
      .cadastrarMusica(request)
      .pipe(
        finalize(() => this.salvando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: response => {
          const mensagem =
            `Música ${response.titulo} cadastrada com sucesso!`;

          this.mensagemSucesso.set(mensagem);

          void this.router.navigate(
            ['/admin/banco/musicas'],
            { state: { mensagemSucesso: mensagem } }
          );
        },
        error: (erro: HttpErrorResponse) => {
          this.mensagemErro.set(
            this.mensagemParaErroDeCadastro(erro)
          );
        }
      });
  }

  private mensagemParaErroDeCadastro(
    erro: HttpErrorResponse
  ): string {
    const mensagemApi = (erro.error as ErroApi | null)?.message;

    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor.';
      case 400:
        return mensagemApi?.trim() ||
          'Existem dados inválidos no formulário.';
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não possui permissão para cadastrar músicas.';
      case 404:
        return mensagemApi?.trim() ||
          'Um dos dados associados não foi encontrado.';
      case 409:
        return mensagemApi?.trim() ||
          'Esta música já está cadastrada.';
      case 500:
        return 'Ocorreu um erro no servidor ao cadastrar a música.';
      default:
        return 'Não foi possível cadastrar a música.';
    }
  }
}
