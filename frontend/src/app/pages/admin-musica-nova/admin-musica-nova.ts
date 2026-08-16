import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import type { ArtistaResponse } from '../../models/ArtistaResponse';
import { AdminArtistaService } from '../../services/admin-artista';

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

  formularioMusica: FormGroup;

  carregando = signal(false);
  mensagemSucesso = signal('');
  mensagemErro = signal('');

  artistas = signal<ArtistaResponse[]>([]);
  carregandoArtistas = signal(false);
  erroArtistas = signal('');

  constructor(
    private readonly fb: FormBuilder,
    private readonly http: HttpClient,
    private readonly router: Router,
    private readonly adminArtistaService: AdminArtistaService
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
      album: [
        '',
        [Validators.required]
      ]
    });
  }

  ngOnInit(): void {
    this.carregarArtistas();
  }

  private carregarArtistas(): void {
    this.carregandoArtistas.set(true);
    this.erroArtistas.set('');

    this.adminArtistaService.listarArtistas().subscribe({
      next: (artistas: ArtistaResponse[]) => {
        this.artistas.set(artistas);
        this.carregandoArtistas.set(false);

        if (artistas.length === 0) {
          this.erroArtistas.set(
            'Nenhum artista cadastrado. Cadastre um artista primeiro.'
          );
        }
      },
      error: () => {
        this.artistas.set([]);
        this.carregandoArtistas.set(false);
        this.erroArtistas.set(
          'Não foi possível carregar a lista de artistas.'
        );
      }
    });
  }

  salvar(): void {
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    if (this.formularioMusica.invalid) {
      this.formularioMusica.markAllAsTouched();
      return;
    }

    const dados = this.montarPayload();

    if (dados === null) {
      this.mensagemErro.set('Selecione um artista válido.');
      return;
    }

    this.carregando.set(true);

    const urlDaApi =
      'http://localhost:8080/api/admin/musicas';

    this.http.post(urlDaApi, dados).subscribe({
      next: () => {
        this.carregando.set(false);
        this.mensagemSucesso.set(
          'Música cadastrada com sucesso!'
        );

        this.formularioMusica.reset();

        this.router.navigate([
          '/admin/banco/musicas'
        ]);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);

        if (erro.status === 400) {
          this.mensagemErro.set(
            'Erro 400: Dados inválidos. Verifique os campos.'
          );
        } else if (erro.status === 401) {
          this.mensagemErro.set(
            'Erro 401: Não autorizado. Faça login novamente.'
          );
        } else if (erro.status === 403) {
          this.mensagemErro.set(
            'Erro 403: Acesso negado. Você não tem permissão.'
          );
        } else if (erro.status === 409) {
          this.mensagemErro.set(
            'Erro 409: Esta música já está cadastrada.'
          );
        } else {
          this.mensagemErro.set(
            'Erro inesperado ao cadastrar a música.'
          );
        }
      }
    });
  }

  private montarPayload() {
    const valores = this.formularioMusica.value;

    const anoLancamento = Number(valores.anoLancamento);
    const artistaPrincipalId = Number(valores.artistaPrincipalId);

    const artistaExiste = this.artistas().some(
      (artista: ArtistaResponse) =>
        artista.idArtista === artistaPrincipalId
    );

    if (!artistaExiste) {
      return null;
    }

    return {
      titulo: valores.titulo,
      duracaoSegundos: Number(valores.duracao),
      anoLancamento,

      artistaPrincipalId,

      artistasParticipantesIds: [],

      album: {
        titulo: valores.album,
        anoLancamento
      },

      generos: [valores.genero]
    };
  }
}
