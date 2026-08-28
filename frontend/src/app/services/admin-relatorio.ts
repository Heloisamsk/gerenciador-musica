import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { RelatorioCatalogo, TipoRelatorio } from '../models/RelatorioCatalogo';

@Injectable({
  providedIn: 'root',
})
export class AdminRelatorioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/admin/relatorios`;

  gerarCatalogo(): Observable<RelatorioCatalogo> {
    return this.http.get<RelatorioCatalogo>(`${this.apiUrl}/catalogo`);
  }

  exportarCatalogo(tipo: TipoRelatorio): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/catalogo.csv`, {
      params: { tipo },
      responseType: 'blob',
    });
  }
}
