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
import { Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';

import { ArtistaRequest } from '../../../models/ArtistaRequest';
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

@Component({
  selector: 'app-cadastro-artista',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './cadastro-artista.html',
  styleUrls: ['./cadastro-artista.css']
})
export class CadastroArtista {

  private readonly formBuilder = inject(FormBuilder);
  private readonly artistaService = inject(AdminArtistaService);

  readonly carregando = signal(false);
  readonly mensagemSucesso = signal('');
  readonly mensagemErro = signal('');

  readonly formulario = this.formBuilder.nonNullable.group({
    nome: [
      '',
      [
        Validators.required,
        naoPermitirApenasEspacos,
        Validators.maxLength(255)
      ]
    ],

    nomeCompleto: [
      '',
      [
        Validators.required,
        naoPermitirApenasEspacos,
        Validators.maxLength(255)
      ]
    ],

    descricao: [
      '',
      [
        Validators.required,
        naoPermitirApenasEspacos,
        Validators.maxLength(500)
      ]
    ],

    fotoPerfilUrl: [
      '',
      [
        Validators.maxLength(2048)
      ]
    ]
  });

  salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    const valores = this.formulario.getRawValue();

    const request: ArtistaRequest = {
      nome: valores.nome.trim(),
      nomeCompleto: valores.nomeCompleto.trim(),
      descricao: valores.descricao.trim(),
      fotoPerfilUrl:
        valores.fotoPerfilUrl.trim() || null
    };

    this.artistaService
      .cadastrar(request)
      .pipe(
        finalize(() => this.carregando.set(false))
      )
      .subscribe({
        next: response => {
          this.mensagemSucesso.set(
            `Artista ${response.nome} cadastrado com sucesso!`
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
              'Você não possui permissão para cadastrar artistas.'
            );
          } else if (erro.status === 409) {
            this.mensagemErro.set(
              'Esse artista já está cadastrado.'
            );
          } else {
            this.mensagemErro.set(
              'Ocorreu um erro ao cadastrar o artista.'
            );
          }
        }
      });
  }
}
