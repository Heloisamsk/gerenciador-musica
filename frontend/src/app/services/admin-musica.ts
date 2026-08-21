import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { MusicaListagem } from '../models/MusicaListagem';
import { PaginaResponse } from '../models/PaginaResponse';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminMusicaService {

  private readonly apiUrl =
    `${environment.apiUrl}/api/musicas`;

  private readonly TAMANHO_PAGINA_MAXIMO = 100;

  constructor(
    private readonly http: HttpClient
  ) {}

  listarMusicas(): Observable<MusicaListagem[]> {
    return this.http
      .get<PaginaResponse<MusicaListagem>>(
        this.apiUrl,
        {
          params: {
            size: this.TAMANHO_PAGINA_MAXIMO
          }
        }
      )
      .pipe(
        map(pagina => pagina.itens)
      );
  }
}
