import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { PlaylistRequest } from '../models/PlaylistRequest';
import { PlaylistResponse } from '../models/PlaylistResponse';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PlaylistService {

  private readonly apiUrl =
    `${environment.apiUrl}/api/playlists`;

  constructor(
    private readonly http: HttpClient
  ) {}

  criar(
    playlist: PlaylistRequest
  ): Observable<PlaylistResponse> {
    return this.http
      .post<PlaylistResponse>(
        this.apiUrl,
        playlist
      )
      .pipe(
        catchError(this.handleError)
      );
  }

  listarMinhas(): Observable<PlaylistResponse[]> {
    return this.http
      .get<PlaylistResponse[]>(this.apiUrl)
      .pipe(
        catchError(this.handleError)
      );
  }

  buscarPorId(
    id: number
  ): Observable<PlaylistResponse> {
    return this.http
      .get<PlaylistResponse>(
        `${this.apiUrl}/${id}`
      )
      .pipe(
        catchError(this.handleError)
      );
  }

  atualizar(
    id: number,
    playlist: PlaylistRequest
  ): Observable<PlaylistResponse> {
    return this.http
      .put<PlaylistResponse>(
        `${this.apiUrl}/${id}`,
        playlist
      )
      .pipe(
        catchError(this.handleError)
      );
  }

  excluir(
    id: number
  ): Observable<void> {
    return this.http
      .delete<void>(
        `${this.apiUrl}/${id}`
      )
      .pipe(
        catchError(this.handleError)
      );
  }

  adicionarMusica(
    playlistId: number,
    musicaId: number
  ): Observable<PlaylistResponse> {
    return this.http
      .post<PlaylistResponse>(
        `${this.apiUrl}/${playlistId}/musicas/${musicaId}`,
        {}
      )
      .pipe(
        catchError(this.handleError)
      );
  }

  removerMusica(
    playlistId: number,
    musicaId: number
  ): Observable<void> {
    return this.http
      .delete<void>(
        `${this.apiUrl}/${playlistId}/musicas/${musicaId}`
      )
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(
    error: HttpErrorResponse
  ) {
    let errorMessage =
      'Ocorreu um erro desconhecido!';

    if (error.error instanceof ErrorEvent) {
      errorMessage =
        `Erro: ${error.error.message}`;
    } else {
      errorMessage =
        `Código do erro: ${error.status}\n` +
        `Mensagem: ${error.message}`;
    }

    console.error(errorMessage);

    return throwError(
      () => new Error(errorMessage)
    );
  }
}
