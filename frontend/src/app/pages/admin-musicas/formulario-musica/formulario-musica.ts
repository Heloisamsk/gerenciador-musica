import { CommonModule } from '@angular/common';
import {
  Component,
  DestroyRef,
  OnInit,
  computed,
  effect,
  inject,
  input,
  output,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {
  catchError,
  finalize,
  of,
  switchMap,
  tap
} from 'rxjs';

import type { AlbumResponse } from '../../../models/AlbumResponse';
import type { ArtistaResponse } from '../../../models/ArtistaResponse';
import type { MusicaRequest } from '../../../models/MusicaRequest';
import type { MusicaResponse } from '../../../models/MusicaResponse';
import { AdminAlbumService } from '../../../services/admin-album.service';
import { AdminArtistaService } from '../../../services/admin-artista';

export type ModoFormularioMusica = 'cadastro' | 'edicao';

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

const validarGeneros: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const valor = control.value;

  if (typeof valor !== 'string') {
    return { generosObrigatorios: true };
  }

  const generos = valor
    .split(',')
    .map(genero => genero.trim().replace(/\s+/g, ' '))
    .filter(genero => genero.length > 0);

  if (generos.length === 0) {
    return { generosObrigatorios: true };
  }

  if (generos.some(genero => genero.length > 100)) {
    return { generoMuitoLongo: true };
  }

  const nomesUnicos = new Set(
    generos.map(genero => genero.toLocaleLowerCase('pt-BR'))
  );

  return nomesUnicos.size === generos.length
    ? null
    : { generoDuplicado: true };
};

@Component({
  selector: 'app-formulario-musica',
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './formulario-musica.html',
  styleUrl: './formulario-musica.css'
})
export class FormularioMusica implements OnInit {

  private readonly formBuilder = inject(FormBuilder);
  private readonly artistaService = inject(AdminArtistaService);
  private readonly albumService = inject(AdminAlbumService);
  private readonly destroyRef = inject(DestroyRef);

  private dadosAplicadosId: number | null = null;

  readonly modo = input<ModoFormularioMusica>('cadastro');
  readonly dadosIniciais = input<MusicaResponse | null>(null);
  readonly carregando = input(false);
  readonly mensagemSucesso = input('');
  readonly mensagemErro = input('');
  readonly exibirCancelar = input(false);

  readonly enviar = output<MusicaRequest>();
  readonly cancelar = output<void>();

  readonly artistas = signal<ArtistaResponse[]>([]);
  readonly albuns = signal<AlbumResponse[]>([]);
  readonly carregandoArtistas = signal(false);
  readonly carregandoAlbuns = signal(false);
  readonly preparandoEdicao = signal(false);
  readonly erroArtistas = signal('');
  readonly erroAlbuns = signal('');
  readonly erroFormulario = signal('');

  readonly titulo = computed(() =>
    this.modo() === 'edicao'
      ? 'Editar música'
      : 'Cadastrar música'
  );

  readonly textoBotao = computed(() =>
    this.modo() === 'edicao'
      ? 'Salvar alterações'
      : 'Cadastrar música'
  );

  readonly textoCarregamento = computed(() =>
    this.modo() === 'edicao'
      ? 'Salvando alterações...'
      : 'Cadastrando música...'
  );

  readonly formularioPronto = computed(() =>
    !this.carregandoArtistas()
      && !this.preparandoEdicao()
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
    letra: [''],
    duracaoSegundos: [
      null as number | null,
      [
        Validators.required,
        Validators.min(1)
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
    generosTexto: [
      '',
      [
        Validators.required,
        validarGeneros
      ]
    ],
    artistaPrincipalId: [
      null as number | null,
      [
        Validators.required,
        Validators.min(1)
      ]
    ],
    artistasParticipantesIds: [[] as number[]],
    albumId: [null as number | null]
  });

  constructor() {
    effect(() => {
      const modo = this.modo();
      const dados = this.dadosIniciais();
      const artistas = this.artistas();

      if (
        modo === 'edicao'
        && dados !== null
        && artistas.length > 0
        && this.dadosAplicadosId !== dados.id
      ) {
        this.preencherDadosDeEdicao(dados);
      } else if (
        modo === 'cadastro'
        && this.dadosAplicadosId !== 0
      ) {
        this.dadosAplicadosId = 0;
        this.resetarFormulario();
      }
    });
  }

  ngOnInit(): void {
    this.configurarAlteracaoDoArtistaPrincipal();
    this.carregarArtistas();
  }

  artistasParticipantesDisponiveis(): ArtistaResponse[] {
    const idPrincipal = Number(
      this.formulario.controls.artistaPrincipalId.value
    );

    return this.artistas().filter(
      artista => artista.idArtista !== idPrincipal
    );
  }

  submeter(): void {
    this.erroFormulario.set('');

    if (
      this.carregando()
      || this.carregandoArtistas()
      || this.carregandoAlbuns()
      || this.preparandoEdicao()
    ) {
      return;
    }

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();
    const artistaPrincipalId = Number(valores.artistaPrincipalId);
    const participantes = valores.artistasParticipantesIds
      .map(id => Number(id));

    if (!this.artistaValido(artistaPrincipalId)) {
      this.erroFormulario.set(
        'Selecione um artista principal válido.'
      );
      return;
    }

    if (!this.participantesValidos(
      participantes,
      artistaPrincipalId
    )) {
      this.erroFormulario.set(
        'O artista principal não pode ser participante da música.'
      );
      return;
    }

    const albumId = valores.albumId === null
      ? null
      : Number(valores.albumId);

    if (!this.albumValido(albumId, artistaPrincipalId)) {
      this.erroFormulario.set(
        'Selecione um álbum pertencente ao artista principal.'
      );
      return;
    }

    if (
      valores.duracaoSegundos === null
      || valores.anoLancamento === null
    ) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.enviar.emit({
      titulo: this.normalizarTexto(valores.titulo),
      letra: valores.letra.trim() || null,
      duracaoSegundos: Number(valores.duracaoSegundos),
      anoLancamento: Number(valores.anoLancamento),
      artistaPrincipalId,
      artistasParticipantesIds: participantes,
      albumId,
      generos: this.normalizarGeneros(valores.generosTexto)
    });
  }

  resetar(): void {
    const dados = this.dadosIniciais();

    if (this.modo() === 'edicao' && dados !== null) {
      this.dadosAplicadosId = null;
      this.preencherDadosDeEdicao(dados);
      return;
    }

    this.resetarFormulario();
  }

  private carregarArtistas(): void {
    this.carregandoArtistas.set(true);
    this.erroArtistas.set('');

    this.artistaService
      .listarArtistas()
      .pipe(
        finalize(() => this.carregandoArtistas.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: artistas => {
          this.artistas.set(artistas);

          if (artistas.length === 0) {
            this.erroArtistas.set(
              'Nenhum artista cadastrado. Cadastre um artista primeiro.'
            );
            this.preparandoEdicao.set(false);
          }
        },
        error: () => {
          this.artistas.set([]);
          this.erroArtistas.set(
            'Não foi possível carregar a lista de artistas.'
          );
          this.preparandoEdicao.set(false);
        }
      });
  }

  private configurarAlteracaoDoArtistaPrincipal(): void {
    this.formulario.controls.artistaPrincipalId.valueChanges
      .pipe(
        tap(valor => {
          const idPrincipal = Number(valor);
          const participantes = this.formulario.controls
            .artistasParticipantesIds.value
            .filter(id => Number(id) !== idPrincipal);

          this.formulario.controls.artistasParticipantesIds
            .setValue(participantes, { emitEvent: false });
          this.formulario.controls.albumId
            .setValue(null, { emitEvent: false });
          this.albuns.set([]);
          this.erroAlbuns.set('');
        }),
        switchMap(valor => {
          const idArtista = Number(valor);

          if (!Number.isSafeInteger(idArtista) || idArtista <= 0) {
            this.carregandoAlbuns.set(false);
            return of([] as AlbumResponse[]);
          }

          this.carregandoAlbuns.set(true);

          return this.albumService
            .listarAlbunsPorArtista(idArtista)
            .pipe(
              catchError(() => {
                this.erroAlbuns.set(
                  'Não foi possível carregar os álbuns desse artista.'
                );
                return of([] as AlbumResponse[]);
              }),
              finalize(() => this.carregandoAlbuns.set(false))
            );
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(albuns => this.albuns.set(albuns));
  }

  private preencherDadosDeEdicao(dados: MusicaResponse): void {
    this.dadosAplicadosId = dados.id;
    this.preparandoEdicao.set(true);
    this.erroFormulario.set('');

    const artistaPrincipalId = dados.artistaPrincipal.id;

    this.formulario.reset(
      {
        titulo: dados.titulo,
        letra: dados.letra ?? '',
        duracaoSegundos: dados.duracaoSegundos,
        anoLancamento: dados.anoLancamento,
        generosTexto: dados.generos
          .map(genero => genero.nome)
          .join(', '),
        artistaPrincipalId,
        artistasParticipantesIds: dados.artistasParticipantes
          .map(artista => artista.id),
        albumId: null
      },
      { emitEvent: false }
    );

    this.carregarAlbunsIniciais(
      artistaPrincipalId,
      dados.album?.id ?? null
    );
  }

  private carregarAlbunsIniciais(
    artistaPrincipalId: number,
    albumInicialId: number | null
  ): void {
    this.carregandoAlbuns.set(true);
    this.erroAlbuns.set('');

    this.albumService
      .listarAlbunsPorArtista(artistaPrincipalId)
      .pipe(
        catchError(() => {
          this.erroAlbuns.set(
            'Não foi possível carregar os álbuns desse artista.'
          );
          return of([] as AlbumResponse[]);
        }),
        finalize(() => {
          this.carregandoAlbuns.set(false);
          this.preparandoEdicao.set(false);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(albuns => {
        this.albuns.set(albuns);

        const albumExiste = albumInicialId === null
          || albuns.some(album =>
            album.idAlbum === albumInicialId
            && album.artista.id === artistaPrincipalId
          );

        this.formulario.controls.albumId.setValue(
          albumExiste ? albumInicialId : null,
          { emitEvent: false }
        );

        if (!albumExiste && !this.erroAlbuns()) {
          this.erroAlbuns.set(
            'O álbum atual não pertence ao artista principal.'
          );
        }
      });
  }

  private resetarFormulario(): void {
    this.formulario.reset({
      titulo: '',
      letra: '',
      duracaoSegundos: null,
      anoLancamento: null,
      generosTexto: '',
      artistaPrincipalId: null,
      artistasParticipantesIds: [],
      albumId: null
    }, { emitEvent: false });
    this.albuns.set([]);
    this.erroAlbuns.set('');
    this.erroFormulario.set('');
  }

  private artistaValido(idArtista: number): boolean {
    return this.artistas().some(
      artista => artista.idArtista === idArtista
    );
  }

  private participantesValidos(
    participantes: number[],
    artistaPrincipalId: number
  ): boolean {
    const idsUnicos = new Set(participantes);

    return idsUnicos.size === participantes.length
      && !idsUnicos.has(artistaPrincipalId)
      && participantes.every(id => this.artistaValido(id));
  }

  private albumValido(
    albumId: number | null,
    artistaPrincipalId: number
  ): boolean {
    return albumId === null || this.albuns().some(
      album => album.idAlbum === albumId
        && album.artista.id === artistaPrincipalId
    );
  }

  private normalizarGeneros(valor: string): string[] {
    return valor
      .split(',')
      .map(genero => this.normalizarTexto(genero))
      .filter(genero => genero.length > 0);
  }

  private normalizarTexto(valor: string): string {
    return valor
      .trim()
      .replace(/\s+/g, ' ');
  }
}
