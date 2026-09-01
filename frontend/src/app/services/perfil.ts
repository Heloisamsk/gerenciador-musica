import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import type {
  AtualizarPerfilRequest,
  PerfilResponse
} from '../models/Perfil';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly apiUrl = `${environment.apiUrl}/api/user/perfil`;
  private readonly apiUrlUsuarios = `${environment.apiUrl}/api/usuarios`;

  constructor(private readonly http: HttpClient) {}

  obter(): Observable<PerfilResponse> {
    return this.http.get<PerfilResponse>(this.apiUrl);
  }

  obterPorId(idUsuario: number): Observable<PerfilResponse> {
    return this.http.get<PerfilResponse>(
      `${this.apiUrlUsuarios}/${idUsuario}/perfil`
    );
  }

  atualizar(dados: AtualizarPerfilRequest): Observable<PerfilResponse> {
    return this.http.put<PerfilResponse>(this.apiUrl, dados);
  }
}
