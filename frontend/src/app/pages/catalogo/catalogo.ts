import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AdminMusicaService } from '../../services/admin-musica';
import { PlaylistService } from '../../services/playlist';
import { MusicaListagem } from '../../models/MusicaListagem';

@Component({
  selector: 'app-catalogo',
  imports: [RouterLink],
  templateUrl: './catalogo.html',
  styleUrls: ['./catalogo.css']
})
export class Catalogo implements OnInit {
  // Signals: este projeto não usa Zone.js, então propriedades comuns
  // alteradas dentro de .subscribe(...) não avisam o Angular pra
  // redesenhar a tela (mesmo bug corrigido em admin-musicas.ts).
  musicas = signal<MusicaListagem[]>([]);
  loadingAdicionar = signal<{ [musicaId: number]: boolean }>({});
  musicasAdicionadas = signal<{ [musicaId: number]: boolean }>({});
  mensagemErro = signal<string | null>(null);
  mensagemSucesso = signal<string | null>(null);

  playlistId!: number;

  constructor(
    private musicaService: AdminMusicaService,
    private playlistService: PlaylistService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.playlistId = Number(this.route.snapshot.paramMap.get('id'));
    this.carregarMusicas();
  }

  carregarMusicas(): void {
    this.musicaService.listarMusicas().subscribe({
      next: (dados) => {
        this.musicas.set(dados);
      },
      error: (err) => {
        this.tratarErro(err, 'Erro ao carregar o catálogo de músicas.');
      }
    });
  }

  adicionarMusica(musicaId: number): void {
    if (this.musicasAdicionadas()[musicaId]) return;

    this.definirLoading(musicaId, true);
    this.mensagemErro.set(null);
    this.mensagemSucesso.set(null);

    this.playlistService.adicionarMusica(this.playlistId, musicaId).subscribe({
      next: () => {
        this.definirAdicionada(musicaId, true);
        this.definirLoading(musicaId, false);
        this.mensagemSucesso.set('Música adicionada com sucesso!');
      },
      error: (err) => {
        this.definirLoading(musicaId, false);
        this.tratarErro(err, 'Não foi possível adicionar a música.', musicaId);
      }
    });
  }

  private tratarErro(err: any, mensagemGenerica: string, musicaId?: number): void {
    if (err.status === 401) {
      this.mensagemErro.set('Sessão expirada ou não autenticada. Faça login novamente.');
    } else if (err.status === 403) {
      this.mensagemErro.set('Você não tem permissão para alterar esta playlist.');
    } else if (err.status === 404) {
      this.mensagemErro.set('Música ou Playlist não encontrada.');
    } else if (err.status === 409) {
      this.mensagemErro.set('Esta música já está na sua playlist!');
      if (musicaId) {
        this.definirAdicionada(musicaId, true);
      }
    } else {
      this.mensagemErro.set(mensagemGenerica);
    }
  }

  // Um signal precisa de um objeto NOVO a cada atualização (o Angular
  // compara por referência), por isso o spread { ...atual } em vez de
  // simplesmente mutar o objeto existente.
  private definirLoading(musicaId: number, valor: boolean): void {
    this.loadingAdicionar.update(atual => ({ ...atual, [musicaId]: valor }));
  }

  private definirAdicionada(musicaId: number, valor: boolean): void {
    this.musicasAdicionadas.update(atual => ({ ...atual, [musicaId]: valor }));
  }
}