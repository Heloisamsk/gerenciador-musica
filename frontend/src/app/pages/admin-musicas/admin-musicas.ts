import {
  Component,
  OnInit,
  computed,
  signal
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { MusicaListagem } from '../../models/MusicaListagem';
import { AdminMusicaService } from '../../services/admin-musica';
import { AcoesMusica } from './acoes-musica/acoes-musica';
import { CapaMusica } from './capa-musica/capa-musica';
import { ParticipantesMusica } from './participantes-musica/participantes-musica';

interface ErroApi {
  message?: string;
}

@Component({
  selector: 'app-admin-musicas',
  imports: [
    RouterLink,
    AcoesMusica,
    CapaMusica,
    ParticipantesMusica
  ],
  templateUrl: './admin-musicas.html',
  styleUrls: ['./admin-musicas.css']
})
export class AdminMusicas implements OnInit {

  private static readonly TAMANHO_PAGINA = 20;

  readonly musicas = signal<MusicaListagem[]>([]);
  readonly paginaAtual = signal(0);
  readonly tamanhoPagina = signal(AdminMusicas.TAMANHO_PAGINA);
  readonly totalItens = signal(0);
  readonly totalPaginas = signal(0);
  readonly carregando = signal(false);
  readonly excluindoId = signal<number | null>(null);
  readonly mensagemErro = signal('');
  readonly mensagemErroExclusao = signal('');
  readonly mensagemSucesso = signal('');
  readonly operacaoEmAndamento = computed(
    () => this.carregando() || this.excluindoId() !== null
  );
  readonly primeiroItemExibido = computed(() =>
    this.totalItens() === 0
      ? 0
      : this.paginaAtual() * this.tamanhoPagina() + 1
  );
  readonly ultimoItemExibido = computed(() => Math.min(
    (this.paginaAtual() + 1) * this.tamanhoPagina(),
    this.totalItens()
  ));

  constructor(
    private readonly adminMusicaService: AdminMusicaService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.recuperarMensagemDaEdicao();
    this.carregarCatalogo();
  }

  carregarCatalogo(pagina = this.paginaAtual()): void {
    if (this.carregando()) {
      return;
    }

    this.carregando.set(true);
    this.mensagemErro.set('');

    this.adminMusicaService
      .listarMusicas(pagina, AdminMusicas.TAMANHO_PAGINA)
      .pipe(
        finalize(() => this.carregando.set(false))
      )
      .subscribe({
        next: dados => {
          this.musicas.set(dados.itens);
          this.paginaAtual.set(dados.paginaAtual);
          this.tamanhoPagina.set(dados.tamanhoPagina);
          this.totalItens.set(dados.totalItens);
          this.totalPaginas.set(dados.totalPaginas);
        },
        error: (erro: HttpErrorResponse) => {
          this.musicas.set([]);
          this.totalItens.set(0);
          this.totalPaginas.set(0);
          this.mensagemErro.set(this.mensagemParaErroDeListagem(erro));
        }
      });
  }

  irParaPagina(pagina: number): void {
    const paginaValida = Number.isInteger(pagina)
      && pagina >= 0
      && pagina < this.totalPaginas();

    if (
      !paginaValida
      || pagina === this.paginaAtual()
      || this.operacaoEmAndamento()
    ) {
      return;
    }

    this.carregarCatalogo(pagina);
  }

  irParaPrimeiraPagina(): void {
    this.irParaPagina(0);
  }

  irParaPaginaAnterior(): void {
    this.irParaPagina(this.paginaAtual() - 1);
  }

  irParaProximaPagina(): void {
    this.irParaPagina(this.paginaAtual() + 1);
  }

  irParaUltimaPagina(): void {
    this.irParaPagina(this.totalPaginas() - 1);
  }

  editarMusica(id: number): void {
    if (this.operacaoEmAndamento()) {
      return;
    }

    void this.router.navigate([
      '/admin/banco/musicas',
      id,
      'editar'
    ]);
  }

  excluirMusica(musica: MusicaListagem): void {
    if (this.operacaoEmAndamento()) {
      return;
    }

    const confirmou = window.confirm(
      `Tem certeza que deseja excluir a música "${musica.titulo}"?`
    );

    if (!confirmou) {
      return;
    }

    this.excluindoId.set(musica.id);
    this.mensagemSucesso.set('');
    this.mensagemErroExclusao.set('');

    this.adminMusicaService
      .excluirMusica(musica.id)
      .pipe(finalize(() => this.excluindoId.set(null)))
      .subscribe({
        complete: () => {
          this.mensagemSucesso.set(
            `Música ${musica.titulo} excluída com sucesso!`
          );
          const paginaAposExclusao =
            this.musicas().length === 1 && this.paginaAtual() > 0
              ? this.paginaAtual() - 1
              : this.paginaAtual();

          this.carregarCatalogo(paginaAposExclusao);
        },
        error: (erro: HttpErrorResponse) => {
          this.mensagemErroExclusao.set(
            this.mensagemParaErroDeExclusao(erro)
          );
        }
      });
  }

  generosTexto(
    musica: MusicaListagem
  ): string {
    if (
      !musica.generos ||
      musica.generos.length === 0
    ) {
      return '-';
    }

    return musica.generos
      .map(genero => genero.nome)
      .join(', ');
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

  private mensagemParaErroDeListagem(
    erro: HttpErrorResponse
  ): string {
    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor.';
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não possui permissão para visualizar as músicas.';
      case 500:
        return 'Ocorreu um erro no servidor. Tente novamente mais tarde.';
      default:
        return 'Não foi possível carregar o catálogo de músicas.';
    }
  }

  private mensagemParaErroDeExclusao(
    erro: HttpErrorResponse
  ): string {
    const mensagemApi = (erro.error as ErroApi | null)?.message;

    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor.';
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não possui permissão para excluir músicas.';
      case 404:
        return 'A música não foi encontrada. Atualize a listagem.';
      case 409:
        return mensagemApi?.trim() ||
          'Não foi possível excluir a música porque ela ainda possui vínculos.';
      case 500:
        return 'Ocorreu um erro no servidor ao excluir a música.';
      default:
        return 'Não foi possível excluir a música.';
    }
  }
}
