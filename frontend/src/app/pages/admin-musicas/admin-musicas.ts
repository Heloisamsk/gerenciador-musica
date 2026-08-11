import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { MusicaResponse } from '../../models/MusicaResponse';
import { AdminMusicaService } from '../../services/admin-musica'; 

@Component({
  selector: 'app-admin-musicas',
  imports: [],
  templateUrl: './admin-musicas.html',
  styleUrls: ['./admin-musicas.css']
})
export class AdminMusicas implements OnInit {
  
  musicas: MusicaResponse[] = []; 
  carregando = false;           // começa falso, só fica true quando for buscar os dados
  mensagemErro = '';            // começa vazia, só preenche se der problema


  constructor(private adminMusicaService: AdminMusicaService) {}
  ngOnInit(): void {
    this.carregarCatalogo();
  }


  carregarCatalogo(): void {
    this.carregando = true; 
    this.mensagemErro = ''; 

    this.adminMusicaService.listarMusicas()
      .pipe(
        finalize(() => this.carregando = false) 
      )
      .subscribe({
        next: (dados) => {
          this.musicas = dados; 
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);
          this.mensagemErro = 'Não foi possível carregar o catálogo de músicas. Tente novamente mais tarde.';
        }
      });
  }
}