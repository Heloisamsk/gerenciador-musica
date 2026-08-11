import { Component } from '@angular/core';
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
  
  carregando = false;
  mensagemSucesso = '';
  mensagemErro = '';

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

    
    this.carregando = true;
    this.mensagemSucesso = '';
    this.mensagemErro = '';

    const urlDaApi = 'http://localhost:8080/api/musicas'; // Ajuste se a sua URL for diferente
    const dados = this.formularioMusica.value;

    this.http.post(urlDaApi, dados).subscribe({
      next: (resposta) => {
        
        this.carregando = false;
        
        
        this.mensagemSucesso = 'Música cadastrada com sucesso!';
        
        
        this.formularioMusica.reset(); 
        
        this.router.navigate(['/admin/banco/musicas']);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando = false;
        
        if (erro.status === 400) {
          this.mensagemErro = 'Erro 400: Dados inválidos. Verifique os campos.';
        } else if (erro.status === 401) {
          this.mensagemErro = 'Erro 401: Não autorizado. Faça login novamente.';
        } else if (erro.status === 403) {
          this.mensagemErro = 'Erro 403: Acesso negado. Você não tem permissão.';
        } else if (erro.status === 409) {
          this.mensagemErro = 'Erro 409: Conflito. Esta música já está cadastrada.';
        } else {
          this.mensagemErro = 'Erro inesperado ao cadastrar a música.';
        }
      }
    });
  }
}