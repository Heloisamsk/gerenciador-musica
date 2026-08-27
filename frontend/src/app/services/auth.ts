import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { finalize, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export type Role = 'USER' | 'ADMIN';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  nome: string;
  email: string;
  role: Role;
}

export interface CadastroRequest {
  nome: string;
  email: string;
  senha: string;
}

export interface CadastroResponse {
  id: number;
  nome: string;
  email: string;
  role: Role;
}

export interface LogoutResponse {
  mensagem: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiBaseUrl =
    `${environment.apiUrl}/api/auth`;

  constructor(
    private readonly http: HttpClient
  ) {}

  login(
    credenciais: LoginRequest
  ): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(
        `${this.apiBaseUrl}/login`,
        credenciais
      )
      .pipe(
        tap(resposta => {
          if (typeof window === 'undefined') {
            return;
          }

          localStorage.setItem(
            'token',
            resposta.token
          );

          localStorage.setItem(
            'role',
            resposta.role
          );

          localStorage.setItem(
            'nome',
            resposta.nome
          );

          localStorage.setItem(
            'email',
            resposta.email
          );

        })
      );
  }

  cadastrar(
    dados: CadastroRequest
  ): Observable<CadastroResponse> {
    return this.http.post<CadastroResponse>(
      `${this.apiBaseUrl}/register`,
      dados
    );
  }

  logout(): Observable<LogoutResponse> {
    return this.http
      .post<LogoutResponse>(
        `${this.apiBaseUrl}/logout`,
        {}
      )
      .pipe(
        finalize(() => this.limparSessao())
      );
  }

  getToken(): string | null {
    if (typeof window === 'undefined') {
      return null;
    }

    return localStorage.getItem('token');
  }

  getRole(): Role | null {
    if (typeof window === 'undefined') {
      return null;
    }

    const role = localStorage.getItem('role');

    if (role === 'USER' || role === 'ADMIN') {
      return role;
    }

    return null;
  }

  isAutenticado(): boolean {
    return this.getToken() !== null;
  }

  limparSessao(): void {
    if (typeof window === 'undefined') {
      return;
    }

    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('nome');
    localStorage.removeItem('email');
  }
}
