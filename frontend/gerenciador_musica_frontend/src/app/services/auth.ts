import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api/auth/login';
  private cadastroUrl = 'http://localhost:8080/api/auth/register';

  constructor(private http: HttpClient) { }

  login(credenciais: any): Observable<any> {
    return this.http
      .post<any>(this.apiUrl, credenciais)
      .pipe(
        tap(resposta => {
          if (
            typeof window !== 'undefined' &&
            resposta?.token
          ) {
            localStorage.setItem(
              'token',
              resposta.token
            );

            if (resposta.role) {
              localStorage.setItem(
                'role',
                resposta.role
              );
            }
          }
        })
      );
  }

cadastrar(dados: any): Observable<any> {
  return this.http.post<any>(this.cadastroUrl, dados);
}

isAutenticado(): boolean {
  return this.getToken() !== null;
}

getToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return localStorage.getItem('token');
}

limparSessao(): void {
  if (typeof window === 'undefined') {
    return;
  }

  cadastrar(dados: any): Observable<any> {
    return this.http.post<any>(
      this.cadastroUrl,
      dados
    );
  }

  getToken(): string | null {
    if (typeof window === 'undefined') {
      return null;
    }

    return localStorage.getItem('token');
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
  }

  logout(): void {
    this.limparSessao();
  }
}
