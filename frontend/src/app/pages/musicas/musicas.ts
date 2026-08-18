import { Component, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { MusicaService } from '../../services/musica';
import { CatalogoService } from '../../services/catalogo';
import { MusicaFiltro } from '../../models/MusicaFiltro';
import { MusicaListagem } from '../../models/MusicaListagem';
import { ArtistaResponse } from '../../models/ArtistaResponse';
import { AlbumResponse } from '../../models/AlbumResponse';
import { GeneroResumo } from '../../models/MusicaResponse';

@Component({
  selector: 'app-musicas',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './musicas.html',
  styleUrls: ['./musicas.css']
})
export class Musicas implements OnInit {

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

  pesquisando = signal(false);
  jaPesquisou = signal(false);
  mensagemErro = signal('');

  constructor(
    private readonly musicaService: MusicaService,
    private readonly catalogoService: CatalogoService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.carregarOpcoesDeFiltro();
    this.restaurarFiltrosDaUrl();
    this.pesquisar();
  }

  pesquisar(): void {
    const filtro = this.montarFiltro();

    this.atualizarQueryParamsDaUrl(filtro);

    this.pesquisando.set(true);
    this.mensagemErro.set('');

    this.musicaService
      .pesquisar(filtro)
      .pipe(
        finalize(() => {
          this.pesquisando.set(false);
          this.jaPesquisou.set(true);
        })
      )
      .subscribe({
        next: (pagina) => {
          this.musicas.set(pagina.itens);
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);
          this.musicas.set([]);
          this.mensagemErro.set(
            'Não foi possível pesquisar as músicas. Tente novamente.'
          );
        }
      });
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
        ano: filtro.ano ?? null
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
