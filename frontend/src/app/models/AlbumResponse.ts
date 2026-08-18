import { ArtistaResumo } from './MusicaResponse';

export interface AlbumResponse {
  idAlbum: number;
  titulo: string;
  anoLancamento: number;
  capaUrl: string | null;
  artista: ArtistaResumo;
}
