import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Params, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { MusicaService } from '../../services/musica';
import { MusicaResponse } from '../../models/MusicaResponse';
import { formatarDuracao } from '../../shared/formatar-duracao';

@Component({
  selector: 'app-musica-detalhe',
  imports: [RouterLink],
  templateUrl: './musica-detalhe.html',
  styleUrls: ['./musica-detalhe.css']
})
export class MusicaDetalhe implements OnInit {

  readonly formatarDuracao = formatarDuracao;

  musica = signal<MusicaResponse | null>(null);
  carregando = signal(false);
  mensagemErro = signal('');

  // Filtros/página da pesquisa que trouxe o usuário até aqui, recebidos via
  // router state (não vão pra URL). Se a página for aberta direto (sem vir
  // da pesquisa), fica vazio e o link de volta cai numa pesquisa em branco.
  voltarQueryParams = signal<Params>({});

  constructor(
    private readonly musicaService: MusicaService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.voltarQueryParams.set(window.history.state?.queryParams ?? {});
    this.carregarMusica(id);
  }

  private carregarMusica(id: number): void {
    this.carregando.set(true);
    this.mensagemErro.set('');

    this.musicaService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (musica) => this.musica.set(musica),
        error: (erro: HttpErrorResponse) => {
          console.error(erro);
          this.mensagemErro.set(this.mensagemDeErroPara(erro));
        }
      });
  }

  private mensagemDeErroPara(erro: HttpErrorResponse): string {
    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.';
      case 404:
        return 'Música não encontrada.';
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não tem permissão para ver essa música.';
      case 500:
        return 'Ocorreu um erro no servidor. Tente novamente mais tarde.';
      default:
        return 'Não foi possível carregar os detalhes da música. Tente novamente.';
    }
  }
}
