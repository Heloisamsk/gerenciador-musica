import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  DestroyRef,
  OnInit,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import {
  catchError,
  finalize,
  of,
  switchMap,
  tap
} from 'rxjs';

import type { AlbumResponse } from '../../models/AlbumResponse';
import type { ArtistaResponse } from '../../models/ArtistaResponse';
import type { MusicaRequest } from '../../models/MusicaRequest';
import { AdminAlbumService } from '../../services/admin-album.service';
import { AdminArtistaService } from '../../services/admin-artista';
import { AdminMusicaService } from '../../services/admin-musica';

interface ErroApi {
  message?: string;
}

@Component({
  selector: 'app-admin-musica-nova',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './admin-musica-nova.html',
  styleUrls: ['./admin-musica-nova.css']
})
export class AdminMusicaNova implements OnInit {

  readonly formularioMusica: FormGroup;

  readonly carregando = signal(false);
  readonly mensagemSucesso = signal('');
  readonly mensagemErro = signal('');

  readonly artistas = signal<ArtistaResponse[]>([]);
  readonly carregandoArtistas = signal(false);
  readonly erroArtistas = signal('');

  readonly albuns = signal<AlbumResponse[]>([]);
  readonly carregandoAlbuns = signal(false);
  readonly erroAlbuns = signal('');

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly adminArtistaService: AdminArtistaService,
    private readonly adminAlbumService: AdminAlbumService,
    private readonly adminMusicaService: AdminMusicaService,
    private readonly destroyRef: DestroyRef
  ) {
    this.formularioMusica = this.fb.group({
      titulo: [
        '',
        [Validators.required]
      ],
      duracao: [
        '',
        [Validators.required, Validators.min(1)]
      ],
      genero: [
        '',
        [Validators.required]
      ],
      anoLancamento: [
        '',
        [
          Validators.required,
          Validators.min(1800),
          Validators.max(2100)
        ]
      ],
      artistaPrincipalId: [
        null,
        [Validators.required]
      ],
      albumId: [null]
    });
  }

  ngOnInit(): void {
    this.configurarCarregamentoDeAlbuns();
    this.carregarArtistas();
  }

  private carregarArtistas(): void {
    this.carregandoArtistas.set(true);
    this.erroArtistas.set('');

    this.adminArtistaService
      .listarArtistas()
      .pipe(
        finalize(() => this.carregandoArtistas.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (artistas: ArtistaResponse[]) => {
          this.artistas.set(artistas);

          if (artistas.length === 0) {
            this.erroArtistas.set(
              'Nenhum artista cadastrado. Cadastre um artista primeiro.'
            );
          }
        },
        error: () => {
          this.artistas.set([]);
          this.erroArtistas.set(
            'Não foi possível carregar a lista de artistas.'
          );
        }
      });
  }

  private configurarCarregamentoDeAlbuns(): void {
    const controleArtista =
      this.formularioMusica.controls['artistaPrincipalId'];
    const controleAlbum =
      this.formularioMusica.controls['albumId'];

    controleArtista.valueChanges
      .pipe(
        tap(() => {
          controleAlbum.reset(null, { emitEvent: false });
          this.albuns.set([]);
          this.erroAlbuns.set('');
        }),
        switchMap(valorArtista => {
          const idArtista = Number(valorArtista);

          if (!Number.isInteger(idArtista) || idArtista <= 0) {
            this.carregandoAlbuns.set(false);
            return of([] as AlbumResponse[]);
          }

          this.carregandoAlbuns.set(true);

          return this.adminAlbumService
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

  salvar(): void {
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    if (this.carregando() || this.carregandoAlbuns()) {
      return;
    }

    if (this.formularioMusica.invalid) {
      this.formularioMusica.markAllAsTouched();
      return;
    }

    const dados = this.montarPayload();

    if (dados === null) {
      this.mensagemErro.set(
        'Selecione um artista e um álbum válidos.'
      );
      return;
    }

    this.carregando.set(true);

    this.adminMusicaService
      .cadastrarMusica(dados)
      .pipe(
        finalize(() => this.carregando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.mensagemSucesso.set(
            'Música cadastrada com sucesso!'
          );

          this.formularioMusica.reset();

          void this.router.navigate([
            '/admin/banco/musicas'
          ]);
        },
        error: (erro: HttpErrorResponse) => {
          const corpo = erro.error as ErroApi | null;

          if (erro.status === 400) {
            this.mensagemErro.set(
              corpo?.message ??
              'Dados inválidos. Verifique os campos.'
            );
          } else if (erro.status === 401) {
            this.mensagemErro.set(
              'Não autorizado. Faça login novamente.'
            );
          } else if (erro.status === 403) {
            this.mensagemErro.set(
              'Acesso negado. Você não tem permissão.'
            );
          } else if (erro.status === 404) {
            this.mensagemErro.set(
              corpo?.message ??
              'O artista ou o álbum selecionado não foi encontrado.'
            );
          } else if (erro.status === 409) {
            this.mensagemErro.set(
              'Esta música já está cadastrada.'
            );
          } else {
            this.mensagemErro.set(
              'Erro inesperado ao cadastrar a música.'
            );
          }
        }
      });
  }

  private montarPayload(): MusicaRequest | null {
    const valores = this.formularioMusica.getRawValue();
    const artistaPrincipalId = Number(valores.artistaPrincipalId);

    const artistaExiste = this.artistas().some(
      artista => artista.idArtista === artistaPrincipalId
    );

    if (!artistaExiste) {
      return null;
    }

    const albumId = valores.albumId === null
      || valores.albumId === ''
      ? null
      : Number(valores.albumId);

    const albumValido = albumId === null
      || this.albuns().some(
        album =>
          album.idAlbum === albumId
          && album.artista.id === artistaPrincipalId
      );

    if (!albumValido) {
      return null;
    }

    return {
      titulo: String(valores.titulo).trim(),
      duracaoSegundos: Number(valores.duracao),
      anoLancamento: Number(valores.anoLancamento),
      artistaPrincipalId,
      artistasParticipantesIds: [],
      albumId,
      generos: [String(valores.genero).trim()]
    };
  }
}
