import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AdminMusicaService } from '../../services/admin-musica';
import { PlaylistService } from '../../services/playlist';
import { MusicaResponse } from '../../models/MusicaResponse';

@Component({
  selector: 'app-catalogo',
  templateUrl: './catalogo.html',
  styleUrls: ['./catalogo.css']
})
export class Catalogo implements OnInit {
  musicas: MusicaResponse[] = [];
  playlistId!: number;
  loadingAdicionar: { [musicaId: number]: boolean } = {};
  musicasAdicionadas: { [musicaId: number]: boolean } = {};
  mensagemErro: string | null = null;
  mensagemSucesso: string | null = null;

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
        this.musicas = dados;
      },
      error: (err) => {
        this.tratarErro(err, 'Erro ao carregar o catálogo de músicas.');
      }
    });
  }


  adicionarMusica(musicaId: number): void {
    if (this.musicasAdicionadas[musicaId]) return;

    this.loadingAdicionar[musicaId] = true;
    this.mensagemErro = null;
    this.mensagemSucesso = null;

    this.playlistService.adicionarMusica(this.playlistId, musicaId).subscribe({
      next: () => {
        this.musicasAdicionadas[musicaId] = true;
        this.loadingAdicionar[musicaId] = false;
        this.mensagemSucesso = 'Música adicionada com sucesso!';
      },
      error: (err) => {
        this.loadingAdicionar[musicaId] = false;
        this.tratarErro(err, 'Não foi possível adicionar a música.', musicaId);
      }
    });
  }


  private tratarErro(err: any, mensagemGenerica: string, musicaId?: number): void {
    if (err.status === 401) {
      this.mensagemErro = 'Sessão expirada ou não autenticada. Faça login novamente.';
    } else if (err.status === 403) {
      this.mensagemErro = 'Você não tem permissão para alterar esta playlist.';
    } else if (err.status === 404) {
      this.mensagemErro = 'Música ou Playlist não encontrada.';
    } else if (err.status === 409) {
      this.mensagemErro = 'Esta música já está na sua playlist!';
      if (musicaId) {
        this.musicasAdicionadas[musicaId] = true;
      }
    } else {
      this.mensagemErro = mensagemGenerica;
    }
  }
}
