import { ChangeDetectorRef, Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { PlaylistRequest } from '../../models/PlaylistRequest';
import { PlaylistService } from '../../services/playlist';

function validarUrlHttp(control: AbstractControl): ValidationErrors | null {
  const valor = String(control.value ?? '').trim();
  if (valor === '' || /^https?:\/\//i.test(valor)) return null;
  return { urlHttp: true };
}

@Component({
  selector: 'app-playlist-nova',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './playlist-nova.html',
  styleUrls: ['./playlist-nova.css']
})
export class PlaylistNova {

  formulario = new FormGroup({
    nome: new FormControl('', [Validators.required]),
    descricao: new FormControl(''),
    capaUrl: new FormControl('', [validarUrlHttp])
  });

  enviando = false;
  mensagemErro = '';

  constructor(
    private readonly playlistService: PlaylistService,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.enviando = true;
    this.mensagemErro = '';

    const capaUrl = this.formulario.value.capaUrl?.trim();

    const dados: PlaylistRequest = {
      nome: this.formulario.value.nome!,
      descricao: this.formulario.value.descricao ?? '',
      capaUrl: capaUrl || null
    };

    this.playlistService.criar(dados)
      .pipe(
        finalize(() => {
          this.enviando = false;
          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: (playlistCriada) => {
          this.router.navigate([
            '/playlists',
            playlistCriada.id
          ]);
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);

          if (erro.status === 401) {
            this.mensagemErro =
              'Sua sessão expirou. Faça login novamente.';
          } else {
            this.mensagemErro =
              'Não foi possível criar a playlist. Tente novamente.';
          }
        }
      });
  }
}
