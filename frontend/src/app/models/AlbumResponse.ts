import { ArtistaResumo } from './ArtistaResumoModel';

export interface AlbumResponse {
  idAlbum: number;
  titulo: string;
  anoLancamento: number;
  capaUrl: string | null;
  artista: ArtistaResumo;
}
