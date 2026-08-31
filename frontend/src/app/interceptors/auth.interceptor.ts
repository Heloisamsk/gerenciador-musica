import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth';
import { environment } from '../../environments/environment';

const API_URL = `${environment.apiUrl}/api`;

const ENDPOINTS_PUBLICOS = new Set([
  `${API_URL}/auth/login`,
  `${API_URL}/auth/register`,
  `${API_URL}/public/albuns/capas`
]);

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();
  const urlSemParametros = request.url.split('?')[0];

  const requisicaoDoBackend = request.url.startsWith(API_URL);
  const endpointPublico = ENDPOINTS_PUBLICOS.has(urlSemParametros);

  let requisicaoAutenticada = request;

  if (token && requisicaoDoBackend && !endpointPublico) {
    requisicaoAutenticada = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(requisicaoAutenticada).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (requisicaoDoBackend && erro.status === 401) {
        authService.limparSessao();
        void router.navigate(['/login']);
      }

      if (requisicaoDoBackend && erro.status === 403) {
        console.warn('Acesso negado: usuário sem permissão.');
      }

      return throwError(() => erro);
    })
  );
};
