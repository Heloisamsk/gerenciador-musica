import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SeguidorService {

  private readonly apiUrl = `${environment.apiUrl}/api`;

  constructor(
    private readonly http: HttpClient
  ) {}

  seguirArtista(artistaId: number): Observable<void> {
    return this.http
      .post<void>(`${this.apiUrl}/artistas/${artistaId}/seguidor`, {})
      .pipe(catchError(this.handleError));
  }

  deixarDeSeguirArtista(artistaId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/artistas/${artistaId}/seguidor`)
      .pipe(catchError(this.handleError));
  }

  seguirUsuario(usuarioId: number): Observable<void> {
    return this.http
      .post<void>(`${this.apiUrl}/usuarios/${usuarioId}/seguidor`, {})
      .pipe(catchError(this.handleError));
  }

  deixarDeSeguirUsuario(usuarioId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/usuarios/${usuarioId}/seguidor`)
      .pipe(catchError(this.handleError));
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
