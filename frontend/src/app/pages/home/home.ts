import {
  Component,
  OnInit,
  signal
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../services/auth';
import { CatalogoService } from '../../services/catalogo';
import { PerfilService } from '../../services/perfil';
import type { AlbumResponse } from '../../models/AlbumResponse';
import type { ArtistaResponse } from '../../models/ArtistaResponse';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.html'
})
export class Home implements OnInit {

  nomeUsuario = signal('Usuário');
  fotoPerfil = signal('/avatar-padrao.png');
  termoBusca = signal('');
  bibliotecaExpandida = signal(false);

  artistas = signal<ArtistaResponse[]>([]);
  albuns = signal<AlbumResponse[]>([]);
  albumDestaque = signal<AlbumResponse | null>(null);

  carregandoArtistas = signal(false);
  carregandoAlbuns = signal(false);

  erroArtistas = signal('');
  erroAlbuns = signal('');

  constructor(
    private readonly authService: AuthService,
    private readonly catalogoService: CatalogoService,
    private readonly perfilService: PerfilService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    if (typeof window !== 'undefined') {
      this.nomeUsuario.set(localStorage.getItem('nome') || 'Usuário');
    }

    this.carregarPerfil();
    this.carregarArtistas();
    this.carregarAlbuns();
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
  }

  // A busca da home não filtra mais localmente: ela leva o usuário até a
  // página de pesquisa de verdade, que já usa os filtros do backend (US06).
  pesquisar(): void {
    const titulo = this.termoBusca().trim();

    this.router.navigate(['/musicas'], {
      queryParams: titulo ? { titulo } : {}
    });
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
        this.fotoPerfil.set(perfil.fotoUrl || '/avatar-padrao.png');
      },
      error: () => this.fotoPerfil.set('/avatar-padrao.png')
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
