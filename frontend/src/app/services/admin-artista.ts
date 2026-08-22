import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ArtistaRequest } from '../models/ArtistaRequest';
import { ArtistaResponse } from '../models/ArtistaResponse';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminArtistaService {

  private readonly apiPublicaUrl =
    `${environment.apiUrl}/api/artistas`;

  private readonly apiAdminUrl =
    `${environment.apiUrl}/api/admin/artistas`;

  constructor(
    private readonly http: HttpClient
  ) {}

  cadastrar(
    artista: ArtistaRequest
  ): Observable<ArtistaResponse> {
    return this.http.post<ArtistaResponse>(
      this.apiAdminUrl,
      artista
    );
  }

  listarArtistas(): Observable<ArtistaResponse[]> {
    return this.http.get<ArtistaResponse[]>(
      this.apiPublicaUrl
    );
  }

  buscarPorId(
    id: number
  ): Observable<ArtistaResponse> {
    return this.http.get<ArtistaResponse>(
      `${this.apiPublicaUrl}/${id}`
    );
  }

  atualizar(
    id: number,
    artista: ArtistaRequest
  ): Observable<ArtistaResponse> {
    return this.http.put<ArtistaResponse>(
      `${this.apiAdminUrl}/${id}`,
      artista
    );
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiAdminUrl}/${id}`
    );
  }
}
