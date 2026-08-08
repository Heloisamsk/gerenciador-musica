import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UsuarioListagem } from '../models/usuario-listagem'; //ta importando é o models

@Injectable({
  providedIn: 'root'
})
export class AdminUsuarioService {
  private readonly apiUrl =
    'http://localhost:8080/api/admin/banco/usuarios';

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<UsuarioListagem[]> {
    return this.http.get<UsuarioListagem[]>(this.apiUrl);
  }
}
