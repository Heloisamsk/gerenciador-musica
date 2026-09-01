import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import type { BuscaResultado } from '../models/BuscaResultado';

@Injectable({
  providedIn: 'root'
})
export class BuscaService {

  private readonly apiUrl = `${environment.apiUrl}/api/busca`;

  constructor(
    private readonly http: HttpClient
  ) {}

  buscar(termo: string): Observable<BuscaResultado> {
    return this.http.get<BuscaResultado>(this.apiUrl, {
      params: { q: termo }
    });
  }
}
