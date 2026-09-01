import {
  Component,
  HostListener,
  OnInit,
  signal
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Subject, finalize, of } from 'rxjs';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  switchMap
} from 'rxjs/operators';

import { AuthService } from '../../services/auth';
import { BuscaService } from '../../services/busca';
import { CatalogoService } from '../../services/catalogo';
import { PerfilService } from '../../services/perfil';
import { PlaylistService } from '../../services/playlist';
import type { AlbumResponse } from '../../models/AlbumResponse';
import type { ArtistaResponse } from '../../models/ArtistaResponse';
import type { BuscaResultado } from '../../models/BuscaResultado';
import type { PlaylistResponse } from '../../models/PlaylistResponse';
import { CurtirBotao } from '../../shared/curtir-botao/curtir-botao';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CurtirBotao],
  templateUrl: './home.html'
})
export class Home implements OnInit {

  nomeUsuario = signal('Usuário');
  fotoPerfil = signal('/avatar-padrao.svg');
  termoBusca = signal('');
  bibliotecaExpandida = signal(false);

  artistas = signal<ArtistaResponse[]>([]);
  albuns = signal<AlbumResponse[]>([]);
  albumDestaque = signal<AlbumResponse | null>(null);
  playlists = signal<PlaylistResponse[]>([]);
  albunsCurtidos = signal<AlbumResponse[]>([]);

  carregandoArtistas = signal(false);
  carregandoAlbuns = signal(false);
  carregandoPlaylists = signal(false);
  carregandoAlbunsCurtidos = signal(false);

  erroArtistas = signal('');
  erroAlbuns = signal('');

  resultadoBusca = signal<BuscaResultado | null>(null);
  buscandoInstantaneo = signal(false);
  mostrarResultadosBusca = signal(false);

  // switchMap garante que só o resultado da busca mais recente é aplicado,
  // mesmo se uma resposta antiga demorar e chegar depois de uma mais nova.
  private readonly buscaInstantanea$ = new Subject<string>();

  constructor(
    private readonly authService: AuthService,
    private readonly buscaService: BuscaService,
    private readonly catalogoService: CatalogoService,
    private readonly perfilService: PerfilService,
    private readonly playlistService: PlaylistService,
    private readonly router: Router
  ) {
    this.buscaInstantanea$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(termo => {
          if (termo.trim().length === 0) {
            return of(null);
          }

          this.buscandoInstantaneo.set(true);

          return this.buscaService.buscar(termo.trim()).pipe(
            catchError(() => of(null))
          );
        })
      )
      .subscribe(resultado => {
        this.buscandoInstantaneo.set(false);
        this.resultadoBusca.set(resultado);
      });
  }

  ngOnInit(): void {
    if (typeof window !== 'undefined') {
      this.nomeUsuario.set(localStorage.getItem('nome') || 'Usuário');
    }

    this.carregarPerfil();
    this.carregarArtistas();
    this.carregarAlbuns();
    this.carregarPlaylists();
    this.carregarAlbunsCurtidos();
  }

  isAdmin(): boolean {
    return this.authService.getRole() === 'ADMIN';
  }

  alternarBiblioteca(): void {
    this.bibliotecaExpandida.update(valor => !valor);
  }

  atualizarBusca(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    this.termoBusca.set(campo.value);
    this.mostrarResultadosBusca.set(campo.value.trim().length > 0);
    this.buscaInstantanea$.next(campo.value);
  }

  // A busca da home continua levando o usuário até a página de pesquisa
  // completa ao pressionar Enter/clicar em Buscar (US06) — o dropdown
  // instantâneo é só uma prévia enquanto o usuário digita.
  pesquisar(): void {
    const titulo = this.termoBusca().trim();

    this.fecharResultadosBusca();

    this.router.navigate(['/musicas'], {
      queryParams: titulo ? { titulo } : {}
    });
  }

  fecharResultadosBusca(): void {
    this.mostrarResultadosBusca.set(false);
  }

  reabrirResultadosBusca(): void {
    if (this.termoBusca().trim().length > 0) {
      this.mostrarResultadosBusca.set(true);
    }
  }

  tratarTeclaBusca(evento: KeyboardEvent): void {
    if (evento.key === 'Escape') {
      this.fecharResultadosBusca();
    }
  }

  @HostListener('document:click', ['$event'])
  aoClicarFora(evento: MouseEvent): void {
    const alvo = evento.target as HTMLElement;

    if (!alvo.closest('.busca-wrapper')) {
      this.fecharResultadosBusca();
    }
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        void this.router.navigate(['/login']);
      },
      error: erro => {
        console.error(
          'Erro ao comunicar o logout ao backend:',
          erro
        );

        void this.router.navigate(['/login']);
      }
    });
  }

  substituirImagem(
    evento: Event,
    imagemPadrao: string
  ): void {
    const imagem = evento.target as HTMLImageElement;

    imagem.onerror = null;
    imagem.src = imagemPadrao;
  }

  private carregarArtistas(): void {
    this.carregandoArtistas.set(true);
    this.erroArtistas.set('');

    this.catalogoService
      .listarArtistas()
      .pipe(
        finalize(() =>
          this.carregandoArtistas.set(false)
        )
      )
      .subscribe({
        next: artistas => {
          this.artistas.set(artistas);
        },
        error: () => {
          this.artistas.set([]);
          this.erroArtistas.set(
            'Não foi possível carregar os artistas.'
          );
        }
      });
  }

  private carregarPerfil(): void {
    this.perfilService.obter().subscribe({
      next: perfil => {
        this.nomeUsuario.set(perfil.nome || 'Usuário');
        this.fotoPerfil.set(perfil.fotoUrl || '/avatar-padrao.svg');
      },
      error: () => this.fotoPerfil.set('/avatar-padrao.svg')
    });
  }

  private carregarAlbuns(): void {
    this.carregandoAlbuns.set(true);
    this.erroAlbuns.set('');

    this.catalogoService
      .listarAlbuns()
      .pipe(
        finalize(() =>
          this.carregandoAlbuns.set(false)
        )
      )
      .subscribe({
        next: albuns => {
          this.albuns.set(albuns);
          this.albumDestaque.set(this.escolherAlbumAleatorio(albuns));
        },
        error: () => {
          this.albuns.set([]);
          this.albumDestaque.set(null);
          this.erroAlbuns.set(
            'Não foi possível carregar os álbuns.'
          );
        }
      });
  }

  private carregarPlaylists(): void {
    this.carregandoPlaylists.set(true);

    this.playlistService
      .listarMinhas()
      .pipe(
        finalize(() =>
          this.carregandoPlaylists.set(false)
        )
      )
      .subscribe({
        next: playlists => {
          this.playlists.set(playlists);
        },
        error: () => {
          this.playlists.set([]);
        }
      });
  }

  private carregarAlbunsCurtidos(): void {
    this.carregandoAlbunsCurtidos.set(true);

    this.catalogoService
      .listarAlbunsCurtidos()
      .pipe(
        finalize(() =>
          this.carregandoAlbunsCurtidos.set(false)
        )
      )
      .subscribe({
        next: albuns => {
          this.albunsCurtidos.set(albuns);
        },
        error: () => {
          this.albunsCurtidos.set([]);
        }
      });
  }

  private escolherAlbumAleatorio(
    albuns: AlbumResponse[]
  ): AlbumResponse | null {
    if (albuns.length === 0) return null;

    const valorAleatorio = new Uint32Array(1);
    globalThis.crypto.getRandomValues(valorAleatorio);
    const indice = valorAleatorio[0] % albuns.length;
    return albuns[indice];
  }

  substituirFotoArtista(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;

    imagem.onerror = null;
    imagem.src = '/avatar-artista.png';
  }
}
