import {
  Component,
  OnInit,
  signal
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { MusicaListagem } from '../../models/MusicaListagem';
import { AdminMusicaService } from '../../services/admin-musica';

@Component({
  selector: 'app-admin-musicas',
  imports: [],
  templateUrl: './admin-musicas.html',
  styleUrls: ['./admin-musicas.css']
})
export class AdminMusicas implements OnInit {

  musicas = signal<MusicaListagem[]>([]);
  carregando = signal(false);
  mensagemErro = signal('');

  constructor(
    private readonly adminMusicaService: AdminMusicaService
  ) {}

  ngOnInit(): void {
    this.carregarCatalogo();
  }

  carregarCatalogo(): void {
    this.carregando.set(true);
    this.mensagemErro.set('');

    this.adminMusicaService.listarMusicas()
      .pipe(
        finalize(() => this.carregando.set(false))
      )
      .subscribe({
        next: (dados) => {
          this.musicas.set(dados);
        },
        error: (erro: HttpErrorResponse) => {
          console.error(erro);

          this.mensagemErro.set(
            'Não foi possível carregar o catálogo de músicas. Tente novamente mais tarde.'
          );
        }
      });
  }

  generosTexto(
    musica: MusicaListagem
  ): string {
    if (
      !musica.generos ||
      musica.generos.length === 0
    ) {
      return '-';
    }

    return musica.generos
      .map(genero => genero.nome)
      .join(', ');
  }
}
