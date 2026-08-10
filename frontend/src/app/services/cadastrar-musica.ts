import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MusicaRequest } from '../models/MusicaRequest';
import { MusicaResponse } from '../models/MusicaResponse';

@Injectable({
  providedIn: 'root'
})
export class MusicaService {
  private readonly API_URL = 'http://localhost:8080/api/musicas';

  constructor(private http: HttpClient) {}

  cadastrar(musica: MusicaRequest): Observable<MusicaResponse> {
    return this.http.post<MusicaResponse>(this.API_URL, musica);
  }

  listar(): Observable<MusicaResponse[]> {
    return this.http.get<MusicaResponse[]>(this.API_URL);
  }

  buscarPorId(id: number): Observable<MusicaResponse> {
    return this.http.get<MusicaResponse>(`${this.API_URL}/${id}`);
  }
}
