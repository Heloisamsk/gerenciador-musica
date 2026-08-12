import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-musica-nova',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-musica-nova.html',
  styleUrls: ['./admin-musica-nova.css']
})
export class AdminMusicaNova {

  formularioMusica: FormGroup;

  // Signals: sem Zone.js neste projeto, uma propriedade comum alterada
  // dentro do .subscribe() não avisa o Angular pra redesenhar a tela.
  // No caminho de sucesso isso ficava escondido porque a tela navega
  // embora, mas no caminho de erro (ex: música duplicada) a mensagem e
  // o botão "Salvando..." ficavam travados pra sempre.
  carregando = signal(false);
  mensagemSucesso = signal('');
  mensagemErro = signal('');

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {
    this.formularioMusica = this.fb.group({
      titulo: ['', [Validators.required]],
      duracao: ['', [Validators.required]],
      genero: ['', [Validators.required]],
      anoLancamento: ['', [Validators.required]],
      artista: ['', [Validators.required]],
      album: ['', [Validators.required]]
    });
  }

  salvar() {
    if (this.formularioMusica.invalid) {
      this.formularioMusica.markAllAsTouched();
      return;
    }


    this.carregando.set(true);
    this.mensagemSucesso.set('');
    this.mensagemErro.set('');

    // Endpoint de cadastro (protegido, exige ROLE_ADMIN) é /api/admin/musicas,
    // diferente do /api/musicas usado só para listagem/consulta.
    const urlDaApi = 'http://localhost:8080/api/admin/musicas';
    const dados = this.montarPayload();

    this.http.post(urlDaApi, dados).subscribe({
      next: (resposta) => {
        this.carregando.set(false);
        this.mensagemSucesso.set('Música cadastrada com sucesso!');
        this.formularioMusica.reset();
        this.router.navigate(['/admin/banco/musicas']);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);

        if (erro.status === 400) {
          this.mensagemErro.set('Erro 400: Dados inválidos. Verifique os campos.');
        } else if (erro.status === 401) {
          this.mensagemErro.set('Erro 401: Não autorizado. Faça login novamente.');
        } else if (erro.status === 403) {
          this.mensagemErro.set('Erro 403: Acesso negado. Você não tem permissão.');
        } else if (erro.status === 409) {
          this.mensagemErro.set('Erro 409: Conflito. Esta música já está cadastrada.');
        } else {
          this.mensagemErro.set('Erro inesperado ao cadastrar a música.');
        }
      }
    });
  }

  /*
   * O formulário é simples (um campo de texto por informação), mas o
   * backend espera um objeto aninhado (MusicaRequestDTO): o artista e o
   * álbum são objetos próprios, e gêneros é uma lista. Esse método traduz
   * um formato pro outro.
   */
  private montarPayload() {
    const valores = this.formularioMusica.value;
    const anoLancamento = Number(valores.anoLancamento);

    return {
      titulo: valores.titulo,
      duracaoSegundos: Number(valores.duracao),
      anoLancamento,
      artistaPrincipal: {
        nome: valores.artista
      },
      artistasParticipantes: [],
      album: {
        titulo: valores.album,
        anoLancamento
      },
      generos: [valores.genero]
    };
  }
}