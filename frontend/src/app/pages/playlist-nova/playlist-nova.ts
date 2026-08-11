import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormGroup, FormControl, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { PlaylistRequest } from '../../models/PlaylistRequest';
import { PlaylistService } from '../../services/playlist';

@Component({
  selector: 'app-playlist-nova',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './playlist-nova.html',
  styleUrls: ['./playlist-nova.css']
})
export class PlaylistNova {

  formulario = new FormGroup({
    nome: new FormControl('', [Validators.required]),
    descricao: new FormControl('')
  });

  enviando = false;
  mensagemErro = '';

  constructor(
    private playlistService: PlaylistService,
    private router: Router
  ) {}

  enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.enviando = true;
    this.mensagemErro = '';

    const dados: PlaylistRequest = {
      nome: this.formulario.value.nome!,
      descricao: this.formulario.value.descricao ?? ''
    };

    this.playlistService.criar(dados)
      .pipe(
        finalize(() => this.enviando = false)
      )
      .subscribe({
        next: (playlistCriada) => {
          this.router.navigate(['/playlists', playlistCriada.id]);
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);

          if (erro.status === 401) {
            this.mensagemErro = 'Sua sessão expirou. Faça login novamente.';
          } else {
            this.mensagemErro = 'Não foi possível criar a playlist. Tente novamente.';
          }
        }
      });
  }
}