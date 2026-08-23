import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AlbumAtualizacaoRequest } from '../models/AlbumAtualizacaoRequest';
import { AlbumRequest } from '../models/AlbumRequestModel';
import { AlbumResponse } from '../models/AlbumResponse';

@Injectable({
  providedIn: 'root'
})
export class AdminAlbumService {

  private readonly apiPublicaUrl =
    `${environment.apiUrl}/api/albuns`;

  private readonly apiAdminUrl =
    `${environment.apiUrl}/api/admin/albuns`;

  constructor(
    private readonly http: HttpClient
  ) {}

  cadastrarAlbum(
    album: AlbumRequest
  ): Observable<AlbumResponse> {
    return this.http.post<AlbumResponse>(
      this.apiAdminUrl,
      album
    );
  }

  listarAlbuns(): Observable<AlbumResponse[]> {
    return this.http.get<AlbumResponse[]>(
      this.apiPublicaUrl
    );
  }

  listarAlbunsPorArtista(
    idArtista: number
  ): Observable<AlbumResponse[]> {
    return this.http.get<AlbumResponse[]>(
      this.apiPublicaUrl,
      {
        params: {
          artistaId: idArtista
        }
      }
    );
  }

  buscarPorId(idAlbum: number): Observable<AlbumResponse> {
    return this.http.get<AlbumResponse>(
      `${this.apiPublicaUrl}/${idAlbum}`
    );
  }

  atualizarAlbum(
    idAlbum: number,
    request: AlbumAtualizacaoRequest
  ): Observable<AlbumResponse> {
    return this.http.put<AlbumResponse>(
      `${this.apiAdminUrl}/${idAlbum}`,
      request
    );
  }

  excluirAlbum(idAlbum: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiAdminUrl}/${idAlbum}`
    );
  }
}
