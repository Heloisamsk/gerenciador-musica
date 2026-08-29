import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import type { AlbumResponse } from '../../models/AlbumResponse';
import type { ArtistaResponse } from '../../models/ArtistaResponse';
import type { MusicaListagem } from '../../models/MusicaListagem';
import type {
  AtualizarPerfilRequest,
  PerfilItem,
  PerfilResponse,
  TipoDestaquePerfil
} from '../../models/Perfil';
import { CatalogoService } from '../../services/catalogo';
import { PerfilService } from '../../services/perfil';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './perfil.html',
  styleUrls: ['./perfil.css']
})
export class Perfil implements OnInit {

  readonly perfil = signal<PerfilResponse | null>(null);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly editando = signal(false);
  readonly catalogoCarregando = signal(false);
  readonly mensagemErro = signal('');
  readonly mensagemSucesso = signal('');

  readonly artistas = signal<ArtistaResponse[]>([]);
  readonly musicas = signal<MusicaListagem[]>([]);
  readonly albuns = signal<AlbumResponse[]>([]);

  readonly favoritos = computed<PerfilItem[]>(() => {
    const perfil = this.perfil();
    if (!perfil) return [];

    return [
      perfil.artistaDestaque,
      perfil.musicaDestaque,
      perfil.albumDestaque
    ].filter((item): item is PerfilItem => item !== null);
  });

  readonly destaquePrincipal = computed<PerfilItem | null>(() => {
    const tipo = this.perfil()?.tipoDestaquePrincipal;
    return this.favoritos().find(item => item.tipo === tipo)
      ?? this.favoritos()[0]
      ?? null;
  });

  readonly favoritosSecundarios = computed(() => {
    const principal = this.destaquePrincipal();
    return this.favoritos().filter(item => item !== principal);
  });

  readonly formulario = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)]
    }),
    username: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.minLength(3),
        Validators.maxLength(30),
        Validators.pattern(/^[A-Za-z0-9._]*$/)
      ]
    }),
    fotoUrl: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(2048), this.validarUrlHttp]
    }),
    bannerUrl: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(2048), this.validarUrlHttp]
    }),
    biografia: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(800)]
    }),
    fraseDestaque: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(160)]
    }),
    idArtistaDestaque: new FormControl<number | null>(null),
    idMusicaDestaque: new FormControl<number | null>(null),
    idAlbumDestaque: new FormControl<number | null>(null),
    tipoDestaquePrincipal: new FormControl<TipoDestaquePerfil | null>(null)
  });

  constructor(
    private readonly perfilService: PerfilService,
    private readonly catalogoService: CatalogoService
  ) {}

  ngOnInit(): void {
    this.carregarPerfil();
  }

  abrirEdicao(): void {
    const perfil = this.perfil();
    if (!perfil) return;

    this.preencherFormulario(perfil);
    this.editando.set(true);
    this.mensagemErro.set('');
    this.mensagemSucesso.set('');

    if (this.artistas().length === 0) {
      this.carregarCatalogo();
    }
  }

  fecharEdicao(): void {
    this.editando.set(false);
    this.mensagemErro.set('');
  }

  salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const dados = this.montarRequest();
    if (!this.destaquePrincipalValido(dados)) {
      this.mensagemErro.set(
        'Escolha como principal um item que esteja nos favoritos do perfil.'
      );
      return;
    }

    this.salvando.set(true);
    this.mensagemErro.set('');

    this.perfilService.atualizar(dados).subscribe({
      next: perfil => {
        this.perfil.set(perfil);
        this.salvando.set(false);
        this.editando.set(false);
        this.mensagemSucesso.set('Perfil atualizado com sucesso.');

        if (typeof window !== 'undefined') {
          localStorage.setItem('nome', perfil.nome);
        }
      },
      error: erro => {
        this.salvando.set(false);
        this.mensagemErro.set(this.extrairMensagemErro(erro));
      }
    });
  }

  rotaItem(item: PerfilItem): string[] {
    const segmento = {
      ARTISTA: 'artistas',
      MUSICA: 'musicas',
      ALBUM: 'albuns'
    }[item.tipo];

    return ['/', segmento, String(item.id)];
  }

  rotuloTipo(tipo: TipoDestaquePerfil): string {
    return {
      ARTISTA: 'Artista favorito',
      MUSICA: 'Música favorita',
      ALBUM: 'Álbum favorito'
    }[tipo];
  }

  imagemAlternativa(tipo: TipoDestaquePerfil): string {
    return tipo === 'ARTISTA' ? '/avatar-artista.png' : '/capa-padrao.png';
  }

  corrigirImagem(evento: Event, fallback: string): void {
    const imagem = evento.currentTarget as HTMLImageElement;
    if (!imagem.src.endsWith(fallback)) {
      imagem.src = fallback;
    }
  }

  private carregarPerfil(): void {
    this.perfilService.obter().subscribe({
      next: perfil => {
        this.perfil.set(perfil);
        this.carregando.set(false);
      },
      error: erro => {
        this.carregando.set(false);
        this.mensagemErro.set(this.extrairMensagemErro(erro));
      }
    });
  }

  private carregarCatalogo(): void {
    this.catalogoCarregando.set(true);

    forkJoin({
      artistas: this.catalogoService.listarArtistas(),
      musicas: this.catalogoService.listarMusicas(),
      albuns: this.catalogoService.listarAlbuns()
    }).subscribe({
      next: catalogo => {
        this.artistas.set(catalogo.artistas);
        this.musicas.set(catalogo.musicas);
        this.albuns.set(catalogo.albuns);
        this.catalogoCarregando.set(false);
      },
      error: () => {
        this.catalogoCarregando.set(false);
        this.mensagemErro.set(
          'Não foi possível carregar o catálogo para escolher os favoritos.'
        );
      }
    });
  }

  private preencherFormulario(perfil: PerfilResponse): void {
    this.formulario.reset({
      nome: perfil.nome,
      username: perfil.username ?? '',
      fotoUrl: perfil.fotoUrl ?? '',
      bannerUrl: perfil.bannerUrl ?? '',
      biografia: perfil.biografia ?? '',
      fraseDestaque: perfil.fraseDestaque ?? '',
      idArtistaDestaque: perfil.artistaDestaque?.id ?? null,
      idMusicaDestaque: perfil.musicaDestaque?.id ?? null,
      idAlbumDestaque: perfil.albumDestaque?.id ?? null,
      tipoDestaquePrincipal: perfil.tipoDestaquePrincipal
    });
  }

  private montarRequest(): AtualizarPerfilRequest {
    const valor = this.formulario.getRawValue();
    return {
      nome: valor.nome.trim(),
      username: this.normalizarOpcional(valor.username),
      fotoUrl: this.normalizarOpcional(valor.fotoUrl),
      bannerUrl: this.normalizarOpcional(valor.bannerUrl),
      biografia: this.normalizarOpcional(valor.biografia),
      fraseDestaque: this.normalizarOpcional(valor.fraseDestaque),
      idArtistaDestaque: valor.idArtistaDestaque,
      idMusicaDestaque: valor.idMusicaDestaque,
      idAlbumDestaque: valor.idAlbumDestaque,
      tipoDestaquePrincipal: valor.tipoDestaquePrincipal
    };
  }

  private destaquePrincipalValido(dados: AtualizarPerfilRequest): boolean {
    if (dados.tipoDestaquePrincipal === null) return true;

    return {
      ARTISTA: dados.idArtistaDestaque !== null,
      MUSICA: dados.idMusicaDestaque !== null,
      ALBUM: dados.idAlbumDestaque !== null
    }[dados.tipoDestaquePrincipal];
  }

  private normalizarOpcional(valor: string): string | null {
    const normalizado = valor.trim();
    return normalizado.length > 0 ? normalizado : null;
  }

  private validarUrlHttp(control: AbstractControl): ValidationErrors | null {
    const valor = String(control.value ?? '').trim();
    if (valor === '' || /^https?:\/\//i.test(valor)) return null;
    return { urlHttp: true };
  }

  private extrairMensagemErro(erro: unknown): string {
    if (erro instanceof HttpErrorResponse) {
      return erro.error?.message
        ?? 'Não foi possível carregar ou atualizar o perfil.';
    }
    return 'Não foi possível carregar ou atualizar o perfil.';
  }
}
