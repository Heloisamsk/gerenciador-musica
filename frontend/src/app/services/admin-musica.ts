import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { MusicaListagem } from '../models/MusicaListagem';
import { PaginaResponse } from '../models/PaginaResponse';
import { MusicaRequest } from '../models/MusicaRequest';
import { MusicaResponse } from '../models/MusicaResponse';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminMusicaService {

  private readonly apiPublicaUrl =
    `${environment.apiUrl}/api/musicas`;

  private readonly apiAdminUrl =
    `${environment.apiUrl}/api/admin/musicas`;

  constructor(
    private readonly http: HttpClient
  ) {}

  listarMusicas(
    pagina = 0,
    tamanhoPagina = 20
  ): Observable<PaginaResponse<MusicaListagem>> {
    return this.http.get<PaginaResponse<MusicaListagem>>(
      this.apiPublicaUrl,
      {
        params: {
          page: pagina,
          size: tamanhoPagina
        }
      }
    );
  }

  cadastrarMusica(
    request: MusicaRequest
  ): Observable<MusicaResponse> {
    return this.http.post<MusicaResponse>(
      this.apiAdminUrl,
      request
    );
  }

  buscarMusicaPorId(
    id: number
  ): Observable<MusicaResponse> {
    return this.http.get<MusicaResponse>(
      `${this.apiPublicaUrl}/${id}`
    );
  }

  atualizarMusica(
    id: number,
    request: MusicaRequest
  ): Observable<MusicaResponse> {
    return this.http.put<MusicaResponse>(
      `${this.apiAdminUrl}/${id}`,
      request
    );
  }

  excluirMusica(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiAdminUrl}/${id}`
    );
  }
}
