import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';
import {
  ActivatedRoute,
  Router,
  RouterLink
} from '@angular/router';
import { finalize } from 'rxjs';

import { AlbumAtualizacaoRequest } from '../../../models/AlbumAtualizacaoRequest';
import { AlbumResponse } from '../../../models/AlbumResponse';
import { AdminAlbumService } from '../../../services/admin-album.service';
import { FormularioAlbum } from '../formulario-album/formulario-album';

interface ErroApi {
  message?: string;
}

@Component({
  selector: 'app-editar-album',
  imports: [FormularioAlbum, RouterLink],
  templateUrl: './editar-album.html',
  styleUrl: './editar-album.css'
})
export class EditarAlbum implements OnInit {

  private readonly albumService = inject(AdminAlbumService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private idAlbum: number | null = null;

  readonly album = signal<AlbumResponse | null>(null);
  readonly carregandoDados = signal(false);
  readonly salvando = signal(false);
  readonly mensagemSucesso = signal('');
  readonly mensagemErro = signal('');

  ngOnInit(): void {
    const id = this.obterIdDaRota();

    if (id === null) {
      this.mensagemErro.set(
        'O identificador do álbum é inválido.'
      );
      return;
    }

    this.idAlbum = id;
    this.carregarAlbum(id);
  }

  salvar(request: AlbumAtualizacaoRequest): void {
    if (this.idAlbum === null || this.salvando()) {
      return;
    }

    this.salvando.set(true);
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    this.albumService
      .atualizarAlbum(this.idAlbum, request)
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: response => {
          this.album.set(response);

          const mensagem =
            `Álbum ${response.titulo} atualizado com sucesso!`;
          this.mensagemSucesso.set(mensagem);

          void this.router.navigate(
            ['/admin/banco/albuns'],
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
      void this.router.navigate(['/admin/banco/albuns']);
    }
  }

  private carregarAlbum(id: number): void {
    this.carregandoDados.set(true);
    this.mensagemErro.set('');

    this.albumService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregandoDados.set(false)))
      .subscribe({
        next: album => this.album.set(album),
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
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não possui permissão para editar álbuns.';
      case 404:
        return 'Álbum não encontrado.';
      case 500:
        return 'Ocorreu um erro no servidor ao carregar o álbum.';
      default:
        return 'Não foi possível carregar os dados do álbum.';
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
        return 'Você não possui permissão para editar álbuns.';
      case 404:
        return 'Álbum não encontrado.';
      case 409:
        return mensagemApi?.trim() ||
          'Já existe outro álbum com esse título, artista e ano.';
      case 500:
        return 'Ocorreu um erro no servidor ao atualizar o álbum.';
      default:
        return 'Não foi possível atualizar o álbum.';
    }
  }
}
