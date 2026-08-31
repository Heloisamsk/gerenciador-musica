import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { environment } from '../../environments/environment';
import type { PaginaResponse } from '../models/PaginaResponse';
import type {
  Review,
  ReviewAtualizacaoRequest,
  ReviewRequest
} from '../models/Review';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  private readonly apiUrl =
    `${environment.apiUrl}/api/reviews`;

  constructor(
    private readonly http: HttpClient
  ) {}

  criar(review: ReviewRequest): Observable<Review> {
    return this.http
      .post<Review>(this.apiUrl, review)
      .pipe(catchError(this.handleError));
  }

  atualizar(
    id: number,
    dados: ReviewAtualizacaoRequest
  ): Observable<Review> {
    return this.http
      .put<Review>(`${this.apiUrl}/${id}`, dados)
      .pipe(catchError(this.handleError));
  }

  excluir(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  buscarPorId(id: number): Observable<Review> {
    return this.http
      .get<Review>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  listarFeed(pagina = 0, tamanho = 60): Observable<PaginaResponse<Review>> {
    return this.http
      .get<PaginaResponse<Review>>(this.apiUrl, {
        params: new HttpParams()
          .set('page', pagina)
          .set('size', tamanho)
      })
      .pipe(catchError(this.handleError));
  }

  listarMinhas(pagina = 0, tamanho = 60): Observable<PaginaResponse<Review>> {
    return this.http
      .get<PaginaResponse<Review>>(`${this.apiUrl}/minhas`, {
        params: new HttpParams()
          .set('page', pagina)
          .set('size', tamanho)
      })
      .pipe(catchError(this.handleError));
  }

  listarPorMusica(
    idMusica: number,
    pagina = 0,
    tamanho?: number
  ): Observable<PaginaResponse<Review>> {
    let params = new HttpParams().set('page', pagina);
    if (tamanho !== undefined) {
      params = params.set('size', tamanho);
    }

    return this.http
      .get<PaginaResponse<Review>>(
        `${this.apiUrl}/musicas/${idMusica}`,
        { params }
      )
      .pipe(catchError(this.handleError));
  }

  listarPorAlbum(
    idAlbum: number,
    pagina = 0
  ): Observable<PaginaResponse<Review>> {
    return this.http
      .get<PaginaResponse<Review>>(
        `${this.apiUrl}/albuns/${idAlbum}`,
        { params: new HttpParams().set('page', pagina) }
      )
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
        error.error?.message ??
        `Código do erro: ${error.status}\n` +
        `Mensagem: ${error.message}`;
    }

    console.error(errorMessage);

    return throwError(
      () => new Error(errorMessage)
    );
  }
}
