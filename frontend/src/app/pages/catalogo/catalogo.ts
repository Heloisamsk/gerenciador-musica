import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MusicaService } from '../../services/musica';
import { PlaylistService } from '../../services/playlist';
import { MusicaListagem } from '../../models/MusicaListagem';
import { forkJoin } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-catalogo',
  imports: [RouterLink],
  templateUrl: './catalogo.html',
  styleUrls: ['./catalogo.css']
})
export class Catalogo implements OnInit {

  musicas = signal<MusicaListagem[]>([]);
  loadingAdicionar = signal<{ [musicaId: number]: boolean }>({});
  musicasAdicionadas = signal<{ [musicaId: number]: boolean }>({});
  mensagemErro = signal<string | null>(null);
  mensagemSucesso = signal<string | null>(null);
  playlistId!: number;

  constructor(
    private readonly musicaService: MusicaService,
    private readonly playlistService: PlaylistService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.playlistId = Number(this.route.snapshot.paramMap.get('id'));
    this.carregarMusicas();
  }

  atualizarBusca(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const termo = campo.value.trim();

    this.musicaService.pesquisar({ titulo: termo }, 0, 100).subscribe({
      next: (pagina) => {
        this.musicas.set(pagina.itens);
      },
      error: (err: HttpErrorResponse) => {
        this.tratarErro(
          err,
          'Erro ao pesquisar músicas no catálogo.'
        );
      }
    });
  }

  carregarMusicas(): void {
    forkJoin({
      catalogo: this.musicaService.pesquisar({}, 0, 100),
      playlist: this.playlistService.buscarPorId(this.playlistId)
    }).subscribe({
      next: (resultados) => {
        this.musicas.set(resultados.catalogo.itens);

        const adicionadas: { [id: number]: boolean } = {};

        resultados.playlist.musicas.forEach(musica => {
          adicionadas[musica.id] = true;
        });

        this.musicasAdicionadas.set(adicionadas);
      },
      error: (err: HttpErrorResponse) => {
        this.tratarErro(
          err,
          'Erro ao carregar os dados do catálogo e da playlist.'
        );
      }
    });
  }

  adicionarMusica(musicaId: number): void {
    if (this.musicasAdicionadas()[musicaId]) {
      return;
    }

    this.definirLoading(musicaId, true);
    this.mensagemErro.set(null);
    this.mensagemSucesso.set(null);

    this.playlistService
      .adicionarMusica(this.playlistId, musicaId)
      .subscribe({
        next: () => {
          this.definirAdicionada(musicaId, true);
          this.definirLoading(musicaId, false);
          this.mensagemSucesso.set(
            'Música adicionada com sucesso!'
          );
        },
        error: (err: HttpErrorResponse) => {
          this.definirLoading(musicaId, false);
          this.tratarErro(
            err,
            'Não foi possível adicionar a música.',
            musicaId
          );
        }
      });
  }

  removerMusica(musicaId: number): void {
    if (!this.musicasAdicionadas()[musicaId]) {
      return;
    }

    this.definirLoading(musicaId, true);
    this.mensagemErro.set(null);
    this.mensagemSucesso.set(null);

    this.playlistService
      .removerMusica(this.playlistId, musicaId)
      .subscribe({
        next: () => {
          this.definirAdicionada(musicaId, false);
          this.definirLoading(musicaId, false);
          this.mensagemSucesso.set(
            'Música removida da playlist com sucesso!'
          );
        },
        error: (err: HttpErrorResponse) => {
          this.definirLoading(musicaId, false);
          this.tratarErro(
            err,
            'Não foi possível remover a música.',
            musicaId
          );
        }
      });
  }

  private tratarErro(
    err: HttpErrorResponse,
    mensagemGenerica: string,
    musicaId?: number
  ): void {
    if (err.status === 401) {
      this.mensagemErro.set(
        'Sessão expirada ou não autenticada. Faça login novamente.'
      );
    } else if (err.status === 403) {
      this.mensagemErro.set(
        'Você não tem permissão para alterar esta playlist.'
      );
    } else if (err.status === 404) {
      this.mensagemErro.set(
        'Música ou Playlist não encontrada.'
      );
    } else if (err.status === 409) {
      this.mensagemErro.set(
        'Esta música já está na sua playlist!'
      );

      if (musicaId) {
        this.definirAdicionada(musicaId, true);
      }
    } else {
      this.mensagemErro.set(mensagemGenerica);
    }
  }

  private definirLoading(
    musicaId: number,
    valor: boolean
  ): void {
    this.loadingAdicionar.update(atual => ({
      ...atual,
      [musicaId]: valor
    }));
  }

  private definirAdicionada(
    musicaId: number,
    valor: boolean
  ): void {
    this.musicasAdicionadas.update(atual => ({
      ...atual,
      [musicaId]: valor
    }));
  }
}
