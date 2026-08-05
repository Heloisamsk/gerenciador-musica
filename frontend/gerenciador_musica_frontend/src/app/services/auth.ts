import { Cadastro } from './../pages/cadastro/cadastro';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api/auth/login';
  private cadastroUrl = 'http://localhost:8080/api/auth/cadastro';

  constructor(private http: HttpClient) { }

  login(credenciais: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, credenciais).pipe(
      tap(resposta => {
        if (resposta && resposta.token) {
          localStorage.setItem('token', resposta.token);

          if (resposta.role) {
            localStorage.setItem('role', resposta.role);
          }
        }
      })
    );
  }

  cadastrar(dados: any): Observable<any> {
    return this.http.post<any>(this.cadastroUrl, dados);
  }

  isAutenticado(): boolean {
    const token = localStorage.getItem('token');
    return token !== null; // Retorna um booleano
  }
}
