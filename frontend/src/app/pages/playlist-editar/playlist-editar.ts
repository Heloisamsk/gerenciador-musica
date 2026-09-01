import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
  selector: 'app-playlist-editar',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './playlist-editar.html',
  styleUrls: ['./playlist-editar.css']
})
export class PlaylistEditar implements OnInit {

  formulario = new FormGroup({
    nome: new FormControl('', [Validators.required]),
    descricao: new FormControl(''),
    capaUrl: new FormControl('', [validarUrlHttp])
  });

  playlistId!: number;
  carregando = false;
  enviando = false;
  excluindo = false;
  mensagemErro = '';

  constructor(
    private readonly playlistService: PlaylistService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.playlistId = Number(this.route.snapshot.paramMap.get('id'));
    this.carregarPlaylist();
  }

  carregarPlaylist(): void {
    this.carregando = true;
    this.mensagemErro = '';

    this.playlistService.buscarPorId(this.playlistId)
      .pipe(
        finalize(() => {
          this.carregando = false;
          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: (playlist) => {
          this.formulario.setValue({
            nome: playlist.nome,
            descricao: playlist.descricao ?? '',
            capaUrl: playlist.capaUrl ?? ''
          });
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);
          this.mensagemErro = this.mensagemDeErroPara(erro);
        }
      });
  }

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

    this.playlistService.atualizar(this.playlistId, dados)
      .pipe(
        finalize(() => {
          this.enviando = false;
          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.router.navigate(['/playlists', this.playlistId]);
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);
          this.mensagemErro = this.mensagemDeErroPara(erro);
        }
      });
  }

  excluir(): void {
    const confirmacao = window.confirm(
      'Tem certeza que deseja excluir esta playlist? Essa ação não pode ser desfeita.'
    );

    if (!confirmacao) {
      return;
    }

    this.excluindo = true;
    this.mensagemErro = '';

    this.playlistService.excluir(this.playlistId)
      .pipe(
        finalize(() => {
          this.excluindo = false;
          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.router.navigate(['/playlists']);
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);
          this.mensagemErro = this.mensagemDeErroPara(erro);
        }
      });
  }

  private mensagemDeErroPara(erro: HttpErrorResponse): string {
    switch (erro.status) {
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não tem permissão para alterar esta playlist.';
      case 404:
        return 'Essa playlist não existe ou foi removida.';
      default:
        return 'Não foi possível salvar as alterações. Tente novamente.';
    }
  }
}
