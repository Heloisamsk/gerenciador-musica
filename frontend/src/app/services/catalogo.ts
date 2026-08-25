import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../environments/environment';
import type { AlbumDetalhe } from '../models/AlbumDetalhe';
import type { AlbumResponse } from '../models/AlbumResponse';
import type { ArtistaDetalhe } from '../models/ArtistaDetalhe';
import type { ArtistaResponse } from '../models/ArtistaResponse';
import type { GeneroResumo } from '../models/MusicaResponse';
import type { MusicaListagem } from '../models/MusicaListagem';
import type { PaginaResponse } from '../models/PaginaResponse';

@Injectable({
  providedIn: 'root'
})
export class CatalogoService {

  private readonly apiUrl =
    `${environment.apiUrl}/api`;

  // Mantém a consulta resumida compatível com a paginação do backend.
  // A gestão completa de músicas utiliza o serviço administrativo,
  // que percorre as páginas sem limitar o catálogo aos primeiros itens.
  private readonly TAMANHO_PAGINA_MAXIMO = 100;

  constructor(
    private readonly http: HttpClient
  ) {}

  listarArtistas(): Observable<ArtistaResponse[]> {
    return this.http.get<ArtistaResponse[]>(
      `${this.apiUrl}/artistas`
    );
  }

  buscarDetalhesArtista(
    idArtista: number
  ): Observable<ArtistaDetalhe> {
    return this.http.get<ArtistaDetalhe>(
      `${this.apiUrl}/artistas/${idArtista}/detalhes`
    );
  }

  listarAlbuns(): Observable<AlbumResponse[]> {
    return this.http.get<AlbumResponse[]>(
      `${this.apiUrl}/albuns`
    );
  }

  buscarDetalhesAlbum(idAlbum: number): Observable<AlbumDetalhe> {
    return this.http.get<AlbumDetalhe>(
      `${this.apiUrl}/albuns/${idAlbum}/detalhes`
    );
  }

  listarGeneros(): Observable<GeneroResumo[]> {
    return this.http.get<GeneroResumo[]>(
      `${this.apiUrl}/generos`
    );
  }

  listarMusicas(): Observable<MusicaListagem[]> {
    return this.http
      .get<PaginaResponse<MusicaListagem>>(`${this.apiUrl}/musicas`, {
        params: { size: this.TAMANHO_PAGINA_MAXIMO }
      })
      .pipe(map(pagina => pagina.itens));
  }
}
