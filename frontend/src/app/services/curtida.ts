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
export class CurtidaService {

  private readonly apiUrl = `${environment.apiUrl}/api`;

  constructor(
    private readonly http: HttpClient
  ) {}

  curtirMusica(musicaId: number): Observable<void> {
    return this.http
      .post<void>(`${this.apiUrl}/musicas/${musicaId}/curtida`, {})
      .pipe(catchError(this.handleError));
  }

  descurtirMusica(musicaId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/musicas/${musicaId}/curtida`)
      .pipe(catchError(this.handleError));
  }

  curtirAlbum(albumId: number): Observable<void> {
    return this.http
      .post<void>(`${this.apiUrl}/albuns/${albumId}/curtida`, {})
      .pipe(catchError(this.handleError));
  }

  descurtirAlbum(albumId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/albuns/${albumId}/curtida`)
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
