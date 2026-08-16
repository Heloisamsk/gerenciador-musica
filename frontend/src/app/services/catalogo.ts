import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import type { ArtistaResponse } from '../models/ArtistaResponse';
import type { MusicaResponse } from '../models/MusicaResponse';

@Injectable({
  providedIn: 'root'
})
export class CatalogoService {

  private readonly apiUrl =
    'http://localhost:8080/api';

  constructor(
    private readonly http: HttpClient
  ) {}

  listarArtistas(): Observable<ArtistaResponse[]> {
    return this.http.get<ArtistaResponse[]>(
      `${this.apiUrl}/artistas`
    );
  }

  listarMusicas(): Observable<MusicaResponse[]> {
    return this.http.get<MusicaResponse[]>(
      `${this.apiUrl}/musicas`
    );
  }
}
