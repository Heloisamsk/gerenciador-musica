import { Component, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { MusicaService } from '../../services/musica';
import { CatalogoService } from '../../services/catalogo';
import { MusicaFiltro } from '../../models/MusicaFiltro';
import { MusicaListagem } from '../../models/MusicaListagem';
import { ArtistaResponse } from '../../models/ArtistaResponse';
import { AlbumResponse } from '../../models/AlbumResponse';
import { GeneroResumo } from '../../models/MusicaResponse';
import { formatarDuracao } from '../../shared/formatar-duracao';
import { CurtirBotao } from '../../shared/curtir-botao/curtir-botao';

@Component({
  selector: 'app-musicas',
  imports: [ReactiveFormsModule, RouterLink, CurtirBotao],
  templateUrl: './musicas.html',
  styleUrls: ['./musicas.css']
})
export class Musicas implements OnInit {

  readonly formatarDuracao = formatarDuracao;

  formulario = new FormGroup({
    titulo: new FormControl(''),
    artistaId: new FormControl<number | null>(null),
    albumId: new FormControl<number | null>(null),
    generoId: new FormControl<number | null>(null),
    ano: new FormControl<number | null>(null)
  });

  // Signals: este projeto não usa Zone.js, então uma atribuição comum
  // dentro do .subscribe(...) não avisa o Angular pra redesenhar a tela.
  musicas = signal<MusicaListagem[]>([]);
  artistas = signal<ArtistaResponse[]>([]);
  albuns = signal<AlbumResponse[]>([]);
  generos = signal<GeneroResumo[]>([]);

  pagina = signal(0);
  totalPaginas = signal(0);
  totalItens = signal(0);

  pesquisando = signal(false);
  jaPesquisou = signal(false);
  mensagemErro = signal('');

  constructor(
    private readonly musicaService: MusicaService,
    private readonly catalogoService: CatalogoService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  // Usado para levar os filtros/página atuais junto no link pra cada música,
  // pra que a página de detalhes consiga montar o link de volta com eles.
  get queryParamsAtuais(): Params {
    return this.route.snapshot.queryParams;
  }

  ngOnInit(): void {
    this.carregarOpcoesDeFiltro();
    this.restaurarFiltrosDaUrl();
    this.executarPesquisa();
  }

  // Chamado pelo formulário (título/filtros) e por "Limpar filtros":
  // sempre volta pra primeira página, porque o conjunto de resultados mudou.
  pesquisar(): void {
    this.pagina.set(0);
    this.executarPesquisa();
  }

  limparFiltros(): void {
    this.formulario.reset({
      titulo: '',
      artistaId: null,
      albumId: null,
      generoId: null,
      ano: null
    });

    this.pesquisar();
  }

  paginaAnterior(): void {
    if (this.pesquisando() || this.pagina() <= 0) {
      return;
    }

    this.pagina.update((atual) => atual - 1);
    this.executarPesquisa();
  }

  proximaPagina(): void {
    if (this.pesquisando() || this.pagina() + 1 >= this.totalPaginas()) {
      return;
    }

    this.pagina.update((atual) => atual + 1);
    this.executarPesquisa();
  }

  // Centraliza a chamada de pesquisa em si; paginação e busca por filtro
  // novo só diferem em resetar (ou não) this.pagina antes de chamar isso.
  private executarPesquisa(): void {
    if (this.pesquisando()) {
      return;
    }

    const filtro = this.montarFiltro();

    this.atualizarQueryParamsDaUrl(filtro);

    this.pesquisando.set(true);
    this.mensagemErro.set('');

    this.musicaService
      .pesquisar(filtro, this.pagina() || undefined)
      .pipe(
        finalize(() => {
          this.pesquisando.set(false);
          this.jaPesquisou.set(true);
        })
      )
      .subscribe({
        next: (resultado) => {
          this.musicas.set(resultado.itens);
          this.totalPaginas.set(resultado.totalPaginas);
          this.totalItens.set(resultado.totalItens);
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);
          this.musicas.set([]);
          this.totalPaginas.set(0);
          this.totalItens.set(0);
          this.mensagemErro.set(this.mensagemDeErroPara(erro));
        }
      });
  }

  private mensagemDeErroPara(erro: HttpErrorResponse): string {
    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.';
      case 401:
        // O interceptor global já limpa a sessão e redireciona pro login.
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não tem permissão para pesquisar músicas.';
      case 500:
        return 'Ocorreu um erro no servidor. Tente novamente mais tarde.';
      default:
        return 'Não foi possível pesquisar as músicas. Tente novamente.';
    }
  }

  private carregarOpcoesDeFiltro(): void {
    this.catalogoService.listarArtistas().subscribe({
      next: (artistas) => this.artistas.set(artistas),
      error: () => this.artistas.set([])
    });

    this.catalogoService.listarAlbuns().subscribe({
      next: (albuns) => this.albuns.set(albuns),
      error: () => this.albuns.set([])
    });

    this.catalogoService.listarGeneros().subscribe({
      next: (generos) => this.generos.set(generos),
      error: () => this.generos.set([])
    });
  }

  private restaurarFiltrosDaUrl(): void {
    const params = this.route.snapshot.queryParamMap;

    this.formulario.patchValue({
      titulo: params.get('titulo') ?? '',
      artistaId: this.paraNumeroOuNulo(params.get('artistaId')),
      albumId: this.paraNumeroOuNulo(params.get('albumId')),
      generoId: this.paraNumeroOuNulo(params.get('generoId')),
      ano: this.paraNumeroOuNulo(params.get('ano'))
    });

    this.pagina.set(this.paraNumeroOuNulo(params.get('page')) ?? 0);
  }

  private montarFiltro(): MusicaFiltro {
    const valores = this.formulario.value;

    return {
      titulo: valores.titulo?.trim() || undefined,
      artistaId: valores.artistaId ?? undefined,
      albumId: valores.albumId ?? undefined,
      generoId: valores.generoId ?? undefined,
      ano: valores.ano ?? undefined
    };
  }

  private atualizarQueryParamsDaUrl(filtro: MusicaFiltro): void {
    // queryParamsHandling: 'merge' + valor null remove o parâmetro da URL,
    // então filtros vazios nunca aparecem na barra de endereço.
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        titulo: filtro.titulo ?? null,
        artistaId: filtro.artistaId ?? null,
        albumId: filtro.albumId ?? null,
        generoId: filtro.generoId ?? null,
        ano: filtro.ano ?? null,
        page: this.pagina() || null
      },
      queryParamsHandling: 'merge',
      replaceUrl: true
    });
  }

  private paraNumeroOuNulo(valor: string | null): number | null {
    if (valor === null || valor === '') {
      return null;
    }

    const numero = Number(valor);
    return Number.isNaN(numero) ? null : numero;
  }
}
