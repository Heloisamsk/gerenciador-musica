import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { finalize } from 'rxjs';

import { AlbumRequest } from '../../../models/AlbumRequestModel';
import { ArtistaResumo } from '../../../models/ArtistaResumoModel';
import { AdminAlbumService } from '../../../services/admin-album.service';
import { AdminArtistaService } from '../../../services/admin-artista';

interface ErroApi {
  message?: string;
  fieldErrors?: Record<string, string>;
}

const naoPermitirApenasEspacos: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const valor = control.value;

  if (typeof valor !== 'string' || valor.length === 0) {
    return null;
  }

  return valor.trim().length === 0
    ? { apenasEspacos: true }
    : null;
};

const urlValida: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const valor = control.value;

  if (typeof valor !== 'string' || valor.trim().length === 0) {
    return null;
  }

  try {
    new URL(valor.trim());
    return null;
  } catch {
    return { urlInvalida: true };
  }
};

@Component({
  selector: 'app-cadastro-album',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './cadastro-album.html',
  styleUrls: ['./cadastro-album.css']
})
export class CadastroAlbum implements OnInit {

  private readonly formBuilder = inject(FormBuilder);
  private readonly albumService = inject(AdminAlbumService);
  private readonly artistaService = inject(AdminArtistaService);

  readonly carregando = signal(false);
  readonly carregandoArtistas = signal(false);
  readonly mensagemSucesso = signal('');
  readonly mensagemErro = signal('');

  artistas: ArtistaResumo[] = [];

  readonly formulario = this.formBuilder.nonNullable.group({
    titulo: [
      '',
      [
        Validators.required,
        naoPermitirApenasEspacos,
        Validators.maxLength(255)
      ]
    ],

    idArtista: [
      null as number | null,
      [
        Validators.required
      ]
    ],

    anoLancamento: [
      null as number | null,
      [
        Validators.required,
        Validators.min(1800),
        Validators.max(2100)
      ]
    ],

    capaUrl: [
      '',
      [
        Validators.maxLength(2048),
        urlValida
      ]
    ]
  });

  ngOnInit(): void {
    this.carregandoArtistas.set(true);

    this.artistaService
      .listarArtistas()
      .pipe(
        finalize(() => this.carregandoArtistas.set(false))
      )
      .subscribe({
        next: (artistas) => this.artistas = artistas,
        error: () => this.artistas = []
      });
  }

  salvar(): void {
    if (this.carregando()) {
        return;
      }

      if (this.formulario.invalid) {
        this.formulario.markAllAsTouched();
        return;
      }
    this.carregando.set(true);
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    const valores = this.formulario.getRawValue();

    const request: AlbumRequest = {
      titulo: valores.titulo.trim(),
      idArtista: valores.idArtista as number,
      anoLancamento: valores.anoLancamento as number,
      capaUrl: valores.capaUrl.trim() || null
    };

    this.albumService
      .cadastrarAlbum(request)
      .pipe(
        finalize(() => this.carregando.set(false))
      )
      .subscribe({
        next: response => {
          this.mensagemSucesso.set(
            `Álbum ${response.titulo} cadastrado com sucesso!`
          );

          this.formulario.reset();
        },

        error: (erro: HttpErrorResponse) => {
          const corpo = erro.error as ErroApi;

          if (erro.status === 400) {
            this.mensagemErro.set(
              corpo.message ??
              'Existem dados inválidos no formulário.'
            );
          } else if (erro.status === 401) {
            this.mensagemErro.set(
              'Sua sessão não é válida. Faça login novamente.'
            );
          } else if (erro.status === 403) {
            this.mensagemErro.set(
              'Você não possui permissão para cadastrar álbuns.'
            );
          } else if (erro.status === 404) {
            this.mensagemErro.set(
              'O artista selecionado não foi encontrado.'
            );
          } else if (erro.status === 409) {
            this.mensagemErro.set(
              'Esse álbum já está cadastrado.'
            );
          } else {
            this.mensagemErro.set(
              'Ocorreu um erro ao cadastrar o álbum.'
            );
          }
        }
      });
  }
}
