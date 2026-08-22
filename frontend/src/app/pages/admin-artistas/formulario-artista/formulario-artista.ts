import { CommonModule } from '@angular/common';
import {
  Component,
  computed,
  effect,
  inject,
  input,
  output
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';

import { ArtistaRequest } from '../../../models/ArtistaRequest';

export type ModoFormularioArtista = 'cadastro' | 'edicao';

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
  selector: 'app-formulario-artista',
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './formulario-artista.html',
  styleUrls: ['./formulario-artista.css']
})
export class FormularioArtista {

  private readonly formBuilder = inject(FormBuilder);

  readonly modo = input<ModoFormularioArtista>('cadastro');
  readonly dadosIniciais = input<ArtistaRequest | null>(null);
  readonly carregando = input(false);
  readonly mensagemSucesso = input('');
  readonly mensagemErro = input('');
  readonly exibirCancelar = input(false);

  readonly enviar = output<ArtistaRequest>();
  readonly cancelar = output<void>();

  readonly titulo = computed(() =>
    this.modo() === 'edicao'
      ? 'Editar artista'
      : 'Cadastrar artista'
  );

  readonly textoBotao = computed(() =>
    this.modo() === 'edicao'
      ? 'Salvar alterações'
      : 'Cadastrar artista'
  );

  readonly textoCarregamento = computed(() =>
    this.modo() === 'edicao'
      ? 'Salvando alterações...'
      : 'Cadastrando artista...'
  );

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
      [Validators.maxLength(2048)]
    ]
  });

  constructor() {
    effect(() => {
      this.preencherFormulario(this.dadosIniciais());
    });
  }

  submeter(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();

    this.enviar.emit({
      nome: this.normalizarCampo(valores.nome),
      nomeCompleto: this.normalizarCampo(valores.nomeCompleto),
      descricao: this.normalizarCampo(valores.descricao),
      fotoPerfilUrl: valores.fotoPerfilUrl.trim() || null
    });
  }

  resetar(): void {
    this.preencherFormulario(this.dadosIniciais());
  }

  private preencherFormulario(
    dados: ArtistaRequest | null
  ): void {
    this.formulario.reset({
      nome: dados?.nome ?? '',
      nomeCompleto: dados?.nomeCompleto ?? '',
      descricao: dados?.descricao ?? '',
      fotoPerfilUrl: dados?.fotoPerfilUrl ?? ''
    });
  }

  private normalizarCampo(valor: string): string {
    return valor
      .trim()
      .replace(/\s+/g, ' ');
  }
}
