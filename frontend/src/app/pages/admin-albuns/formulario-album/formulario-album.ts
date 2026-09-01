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

import { AlbumAtualizacaoRequest } from '../../../models/AlbumAtualizacaoRequest';
import { AlbumRequest } from '../../../models/AlbumRequestModel';
import { AlbumResponse } from '../../../models/AlbumResponse';
import { ArtistaResponse } from '../../../models/ArtistaResponse';
import { RouterLink } from '@angular/router';

export type ModoFormularioAlbum = 'cadastro' | 'edicao';

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
    const url = new URL(valor.trim());

    return url.protocol === 'http:' || url.protocol === 'https:'
      ? null
      : { urlInvalida: true };
  } catch {
    return { urlInvalida: true };
  }
};

@Component({
  selector: 'app-formulario-album',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './formulario-album.html',
  styleUrl: './formulario-album.css'
})
export class FormularioAlbum {

  private readonly formBuilder = inject(FormBuilder);

  readonly modo = input<ModoFormularioAlbum>('cadastro');
  readonly dadosIniciais = input<AlbumResponse | null>(null);
  readonly artistas = input<ArtistaResponse[]>([]);
  readonly carregandoArtistas = input(false);
  readonly erroArtistas = input('');
  readonly carregando = input(false);
  readonly mensagemSucesso = input('');
  readonly mensagemErro = input('');
  readonly exibirCancelar = input(false);

  readonly enviarCadastro = output<AlbumRequest>();
  readonly enviarEdicao = output<AlbumAtualizacaoRequest>();
  readonly cancelar = output<void>();

  readonly titulo = computed(() =>
    this.modo() === 'edicao'
      ? 'Editar álbum'
      : 'Cadastrar álbum'
  );

  readonly textoBotao = computed(() =>
    this.modo() === 'edicao'
      ? 'Salvar alterações'
      : 'Cadastrar álbum'
  );

  readonly textoCarregamento = computed(() =>
    this.modo() === 'edicao'
      ? 'Salvando alterações...'
      : 'Cadastrando álbum...'
  );

  readonly artistaResponsavel = computed(
    () => this.dadosIniciais()?.artista ?? null
  );

  readonly cadastroIndisponivel = computed(() =>
    this.modo() === 'cadastro' && (
      this.carregandoArtistas() || this.artistas().length === 0
    )
  );

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
      [Validators.required]
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

  constructor() {
    effect(() => {
      this.preencherFormulario(this.dadosIniciais());
    });
  }

  submeter(): void {
    if (this.carregando()) {
      return;
    }

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();

    if (valores.anoLancamento === null) {
      this.formulario.markAllAsTouched();
      return;
    }

    const request: AlbumAtualizacaoRequest = {
      titulo: this.normalizarTitulo(valores.titulo),
      anoLancamento: valores.anoLancamento,
      capaUrl: valores.capaUrl.trim() || null
    };

    if (this.modo() === 'edicao') {
      this.enviarEdicao.emit(request);
      return;
    }

    if (valores.idArtista === null) {
      this.formulario.controls.idArtista.markAsTouched();
      return;
    }

    this.enviarCadastro.emit({
      ...request,
      idArtista: valores.idArtista
    });
  }

  resetar(): void {
    this.preencherFormulario(this.dadosIniciais());
  }

  private preencherFormulario(dados: AlbumResponse | null): void {
    this.formulario.reset({
      titulo: dados?.titulo ?? '',
      idArtista: dados?.artista.id ?? null,
      anoLancamento: dados?.anoLancamento ?? null,
      capaUrl: dados?.capaUrl ?? ''
    });
  }

  private normalizarTitulo(valor: string): string {
    return valor
      .trim()
      .replace(/\s+/g, ' ');
  }
}
