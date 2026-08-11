import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PlaylistRequest } from '../models/PlaylistRequest';
import { PlaylistResponse } from '../models/PlaylistResponse';

@Injectable({
  providedIn: 'root'
})

export class PlaylistService {
  private apiUrl = 'http://localhost:8080/playlists';

  constructor(private http: HttpClient) { }

  criar(playlist: PlaylistRequest): Observable<PlaylistResponse> {
    return this.http.post<PlaylistResponse>(this.apiUrl, playlist)
      .pipe(catchError(this.handleError));
  }

  listarMinhas(): Observable<PlaylistResponse[]> {
    return this.http.get<PlaylistResponse[]>(`${this.apiUrl}/minhas`)
      .pipe(catchError(this.handleError));
  }

  buscarPorId(id: number): Observable<PlaylistResponse> {
    return this.http.get<PlaylistResponse>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  adicionarMusica(playlistId: number, musicaId: number): Observable<PlaylistResponse> {
    return this.http.post<PlaylistResponse>(`${this.apiUrl}/${playlistId}/musicas/${musicaId}`, {})
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Ocorreu um erro desconhecido!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erro: ${error.error.message}`;
    } else {
      errorMessage = `Código do erro: ${error.status}\nMensagem: ${error.message}`;
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
