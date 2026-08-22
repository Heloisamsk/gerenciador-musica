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

import { ArtistaRequest } from '../../../models/ArtistaRequest';
import { ArtistaResponse } from '../../../models/ArtistaResponse';
import { AdminArtistaService } from '../../../services/admin-artista';
import { FormularioArtista } from '../formulario-artista/formulario-artista';

@Component({
  selector: 'app-editar-artista',
  imports: [FormularioArtista, RouterLink],
  templateUrl: './editar-artista.html',
  styleUrl: './editar-artista.css'
})
export class EditarArtista implements OnInit {

  private readonly artistaService = inject(AdminArtistaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private idArtista: number | null = null;

  readonly dadosIniciais = signal<ArtistaRequest | null>(null);
  readonly carregandoDados = signal(false);
  readonly salvando = signal(false);
  readonly mensagemSucesso = signal('');
  readonly mensagemErro = signal('');

  ngOnInit(): void {
    const id = this.obterIdDaRota();

    if (id === null) {
      this.mensagemErro.set(
        'O identificador do artista é inválido.'
      );
      return;
    }

    this.idArtista = id;
    this.carregarArtista(id);
  }

  salvar(request: ArtistaRequest): void {
    if (this.idArtista === null || this.salvando()) {
      return;
    }

    this.salvando.set(true);
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    this.artistaService
      .atualizar(this.idArtista, request)
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: response => {
          this.dadosIniciais.set(this.converterParaRequest(response));

          const mensagem =
            `Artista ${response.nome} atualizado com sucesso!`;
          this.mensagemSucesso.set(mensagem);

          void this.router.navigate(
            ['/admin/banco/artistas'],
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
      void this.router.navigate(['/admin/banco/artistas']);
    }
  }

  private carregarArtista(id: number): void {
    this.carregandoDados.set(true);
    this.mensagemErro.set('');

    this.artistaService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregandoDados.set(false)))
      .subscribe({
        next: artista => {
          this.dadosIniciais.set(this.converterParaRequest(artista));
        },
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

  private converterParaRequest(
    artista: ArtistaResponse
  ): ArtistaRequest {
    return {
      nome: artista.nome,
      nomeCompleto: artista.nomeCompleto,
      descricao: artista.descricao,
      fotoPerfilUrl: artista.fotoPerfilUrl
    };
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
        return 'Você não possui permissão para editar artistas.';
      case 404:
        return 'Artista não encontrado.';
      case 500:
        return 'Ocorreu um erro no servidor ao carregar o artista.';
      default:
        return 'Não foi possível carregar os dados do artista.';
    }
  }

  private mensagemParaErroDeAtualizacao(
    erro: HttpErrorResponse
  ): string {
    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor.';
      case 400:
        return 'Existem dados inválidos no formulário.';
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não possui permissão para editar artistas.';
      case 404:
        return 'Artista não encontrado.';
      case 409:
        return 'Já existe outro artista com esse nome.';
      case 500:
        return 'Ocorreu um erro no servidor ao atualizar o artista.';
      default:
        return 'Não foi possível atualizar o artista.';
    }
  }
}
