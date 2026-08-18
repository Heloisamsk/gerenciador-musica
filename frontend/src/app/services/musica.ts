import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { MusicaFiltro } from '../models/MusicaFiltro';
import { MusicaListagem } from '../models/MusicaListagem';
import { MusicaResponse } from '../models/MusicaResponse';
import { PaginaResponse } from '../models/PaginaResponse';

// Encapsula as requisições HTTP do catálogo público de músicas (US06).
// O interceptor JWT já é aplicado globalmente (ver app.config.ts), então
// nenhuma configuração extra de autenticação é necessária aqui.
@Injectable({
  providedIn: 'root'
})
export class MusicaService {

  private readonly apiUrl = 'http://localhost:8080/api/musicas';

  constructor(private readonly http: HttpClient) {}

  pesquisar(
    filtro: MusicaFiltro = {},
    pagina?: number,
    tamanho?: number,
    sort?: string
  ): Observable<PaginaResponse<MusicaListagem>> {
    const params = this.montarParametros(filtro, pagina, tamanho, sort);

    return this.http.get<PaginaResponse<MusicaListagem>>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<MusicaResponse> {
    return this.http.get<MusicaResponse>(`${this.apiUrl}/${id}`);
  }

  private montarParametros(
    filtro: MusicaFiltro,
    pagina?: number,
    tamanho?: number,
    sort?: string
  ): HttpParams {
    let params = new HttpParams();

    params = this.adicionarSeNaoVazio(params, 'titulo', filtro.titulo);
    params = this.adicionarSeNaoVazio(params, 'artistaId', filtro.artistaId);
    params = this.adicionarSeNaoVazio(params, 'albumId', filtro.albumId);
    params = this.adicionarSeNaoVazio(params, 'generoId', filtro.generoId);
    params = this.adicionarSeNaoVazio(params, 'ano', filtro.ano);
    params = this.adicionarSeNaoVazio(params, 'page', pagina);
    params = this.adicionarSeNaoVazio(params, 'size', tamanho);
    params = this.adicionarSeNaoVazio(params, 'sort', sort);

    return params;
  }

  // Nunca envia um parâmetro nulo, indefinido ou composto só de espaços —
  // o backend trata a ausência do parâmetro como "sem filtro".
  private adicionarSeNaoVazio(
    params: HttpParams,
    nome: string,
    valor: string | number | undefined
  ): HttpParams {
    if (valor === undefined || valor === null) {
      return params;
    }

    if (typeof valor === 'string' && valor.trim() === '') {
      return params;
    }

    return params.set(nome, valor);
  }
}
