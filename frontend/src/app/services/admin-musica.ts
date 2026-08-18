import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { MusicaResponse } from '../models/MusicaResponse';
import { PaginaResponse } from '../models/PaginaResponse';

@Injectable({
  providedIn: 'root'
})
export class AdminMusicaService {

  private apiUrl = 'http://localhost:8080/api/musicas';

  // GET /api/musicas agora devolve uma resposta paginada (US06). Esta tela
  // ainda não tem paginação própria, então pedimos o tamanho máximo de
  // página que o backend aceita (100). Catálogos maiores que isso precisam
  // de paginação real na tela, não só neste serviço.
  private readonly TAMANHO_PAGINA_MAXIMO = 100;

  constructor(private http: HttpClient) {}

  listarMusicas(): Observable<MusicaResponse[]> {
    return this.http
      .get<PaginaResponse<MusicaResponse>>(this.apiUrl, {
        params: { size: this.TAMANHO_PAGINA_MAXIMO }
      })
      .pipe(map(pagina => pagina.itens));
  }
}