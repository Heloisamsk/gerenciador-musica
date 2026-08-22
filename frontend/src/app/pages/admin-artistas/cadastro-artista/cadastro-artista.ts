import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  inject,
  signal,
  viewChild
} from '@angular/core';
import { finalize } from 'rxjs';

import { ArtistaRequest } from '../../../models/ArtistaRequest';
import { AdminArtistaService } from '../../../services/admin-artista';
import { FormularioArtista } from '../formulario-artista/formulario-artista';

interface ErroApi {
  message?: string;
  fieldErrors?: Record<string, string>;
}

@Component({
  selector: 'app-cadastro-artista',
  imports: [FormularioArtista],
  templateUrl: './cadastro-artista.html'
})
export class CadastroArtista {

  private readonly artistaService = inject(AdminArtistaService);
  private readonly formularioArtista = viewChild(FormularioArtista);

  readonly carregando = signal(false);
  readonly mensagemSucesso = signal('');
  readonly mensagemErro = signal('');

  salvar(request: ArtistaRequest): void {
    this.carregando.set(true);
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    this.artistaService
      .cadastrar(request)
      .pipe(
        finalize(() => this.carregando.set(false))
      )
      .subscribe({
        next: response => {
          this.mensagemSucesso.set(
            `Artista ${response.nome} cadastrado com sucesso!`
          );

          this.formularioArtista()?.resetar();
        },

        error: (erro: HttpErrorResponse) => {
          const corpo = erro.error as ErroApi;

          if (erro.status === 400) {
            this.mensagemErro.set(
              corpo.message ??
              'Existem dados inválidos no formulário.'
            );
          } else if (erro.status === 401) {
            this.mensagemErro.set(
              'Sua sessão não é válida. Faça login novamente.'
            );
          } else if (erro.status === 403) {
            this.mensagemErro.set(
              'Você não possui permissão para cadastrar artistas.'
            );
          } else if (erro.status === 409) {
            this.mensagemErro.set(
              'Esse artista já está cadastrado.'
            );
          } else {
            this.mensagemErro.set(
              'Ocorreu um erro ao cadastrar o artista.'
            );
          }
        }
      });
  }
}
