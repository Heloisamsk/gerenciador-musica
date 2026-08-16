import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ArtistaRequest } from '../models/ArtistaRequest';
import { ArtistaResponse } from '../models/ArtistaResponse';

@Injectable({
  providedIn: 'root'
})
export class AdminArtistaService {

  private readonly cadastroApiUrl =
    'http://localhost:8080/api/admin/artistas';

  private readonly listagemApiUrl =
    'http://localhost:8080/api/artistas';

  constructor(
    private readonly http: HttpClient
  ) {}

  cadastrar(
    artista: ArtistaRequest
  ): Observable<ArtistaResponse> {
    return this.http.post<ArtistaResponse>(
      this.cadastroApiUrl,
      artista
    );
  }

  listarArtistas(): Observable<ArtistaResponse[]> {
    return this.http.get<ArtistaResponse[]>(
      this.listagemApiUrl
    );
  }
}
