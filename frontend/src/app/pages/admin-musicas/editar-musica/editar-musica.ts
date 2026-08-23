import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ActivatedRoute,
  Router,
  RouterLink
} from '@angular/router';
import { finalize } from 'rxjs';

import type { MusicaRequest } from '../../../models/MusicaRequest';
import type { MusicaResponse } from '../../../models/MusicaResponse';
import { AdminMusicaService } from '../../../services/admin-musica';
import { FormularioMusica } from '../formulario-musica/formulario-musica';

interface ErroApi {
  message?: string;
}

@Component({
  selector: 'app-editar-musica',
  imports: [FormularioMusica, RouterLink],
  templateUrl: './editar-musica.html',
  styleUrl: './editar-musica.css'
})
export class EditarMusica implements OnInit {

  private readonly musicaService = inject(AdminMusicaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  private idMusica: number | null = null;

  readonly musica = signal<MusicaResponse | null>(null);
  readonly carregandoDados = signal(false);
  readonly salvando = signal(false);
  readonly mensagemSucesso = signal('');
  readonly mensagemErro = signal('');

  ngOnInit(): void {
    const id = this.obterIdDaRota();

    if (id === null) {
      this.mensagemErro.set(
        'O identificador da música é inválido.'
      );
      return;
    }

    this.idMusica = id;
    this.carregarMusica(id);
  }

  salvar(request: MusicaRequest): void {
    if (this.idMusica === null || this.salvando()) {
      return;
    }

    this.salvando.set(true);
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    this.musicaService
      .atualizarMusica(this.idMusica, request)
      .pipe(
        finalize(() => this.salvando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: response => {
          this.musica.set(response);

          const mensagem =
            `Música ${response.titulo} atualizada com sucesso!`;
          this.mensagemSucesso.set(mensagem);

          void this.router.navigate(
            ['/admin/banco/musicas'],
            { state: { mensagemSucesso: mensagem } }
          );
        },
        error: (erro: HttpErrorResponse) => {
          this.mensagemErro.set(
            this.mensagemParaErroDeAtualizacao(erro)
          );
        }
      });
  }

  cancelar(): void {
    if (!this.salvando()) {
      void this.router.navigate(['/admin/banco/musicas']);
    }
  }

  private carregarMusica(id: number): void {
    this.carregandoDados.set(true);
    this.mensagemErro.set('');

    this.musicaService
      .buscarMusicaPorId(id)
      .pipe(
        finalize(() => this.carregandoDados.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: musica => this.musica.set(musica),
        error: (erro: HttpErrorResponse) => {
          this.mensagemErro.set(
            this.mensagemParaErroDeConsulta(erro)
          );
        }
      });
  }

  private obterIdDaRota(): number | null {
    const parametro = this.route.snapshot.paramMap.get('id');

    if (parametro === null || !/^\d+$/.test(parametro)) {
      return null;
    }

    const id = Number(parametro);

    return Number.isSafeInteger(id) && id > 0
      ? id
      : null;
  }

  private mensagemParaErroDeConsulta(
    erro: HttpErrorResponse
  ): string {
    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor.';
      case 400:
        return 'O identificador da música é inválido.';
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não possui permissão para editar músicas.';
      case 404:
        return 'Música não encontrada.';
      case 500:
        return 'Ocorreu um erro no servidor ao carregar a música.';
      default:
        return 'Não foi possível carregar os dados da música.';
    }
  }

  private mensagemParaErroDeAtualizacao(
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
        return 'Você não possui permissão para editar músicas.';
      case 404:
        return mensagemApi?.trim() || 'Música não encontrada.';
      case 409:
        return mensagemApi?.trim() ||
          'Já existe outra música com esses dados.';
      case 500:
        return 'Ocorreu um erro no servidor ao atualizar a música.';
      default:
        return 'Não foi possível atualizar a música.';
    }
  }
}
