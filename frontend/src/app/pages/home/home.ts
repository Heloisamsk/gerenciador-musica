import {
  Component,
  OnInit,
  signal
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../services/auth';
import { CatalogoService } from '../../services/catalogo';
import type { ArtistaResponse } from '../../models/ArtistaResponse';
import type { MusicaListagem } from '../../models/MusicaListagem';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {

  nomeUsuario = signal('Usuário');
  termoBusca = signal('');

  artistas = signal<ArtistaResponse[]>([]);
  musicas = signal<MusicaListagem[]>([]);

  carregandoArtistas = signal(false);
  carregandoMusicas = signal(false);

  erroArtistas = signal('');
  erroMusicas = signal('');

  constructor(
    private readonly authService: AuthService,
    private readonly catalogoService: CatalogoService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.carregarArtistas();
    this.carregarMusicas();
  }

  isAdmin(): boolean {
    return this.authService.getRole() === 'ADMIN';
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

  private carregarMusicas(): void {
    this.carregandoMusicas.set(true);
    this.erroMusicas.set('');

    this.catalogoService
      .listarMusicas()
      .pipe(
        finalize(() =>
          this.carregandoMusicas.set(false)
        )
      )
      .subscribe({
        next: musicas => {
          this.musicas.set(musicas);
        },
        error: () => {
          this.musicas.set([]);
          this.erroMusicas.set(
            'Não foi possível carregar as músicas.'
          );
        }
      });
  }

  substituirFotoArtista(evento: Event): void {
    const imagem = evento.target as HTMLImageElement;

    imagem.onerror = null;
    imagem.src = '/avatar-artista.png';
  }
}
