import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import type { ArtistaResponse } from '../models/ArtistaResponse';
import type { MusicaListagem } from '../models/MusicaListagem';
import type { PaginaResponse } from '../models/PaginaResponse';

@Injectable({
  providedIn: 'root'
})
export class CatalogoService {

  private readonly apiUrl =
    'http://localhost:8080/api';

  // GET /api/musicas agora devolve uma resposta paginada (US06). A home
  // ainda filtra a lista inteira no cliente, então pedimos o tamanho
  // máximo de página que o backend aceita (100) — um catálogo maior que
  // isso precisaria passar a usar a pesquisa do backend de verdade.
  private readonly TAMANHO_PAGINA_MAXIMO = 100;

  constructor(
    private readonly http: HttpClient
  ) {}

  listarArtistas(): Observable<ArtistaResponse[]> {
    return this.http.get<ArtistaResponse[]>(
      `${this.apiUrl}/artistas`
    );
  }

  listarMusicas(): Observable<MusicaListagem[]> {
    return this.http
      .get<PaginaResponse<MusicaListagem>>(`${this.apiUrl}/musicas`, {
        params: { size: this.TAMANHO_PAGINA_MAXIMO }
      })
      .pipe(map(pagina => pagina.itens));
  }
}
