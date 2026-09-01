import type { AlbumCatalogo } from './ArtistaDetalhe';

export interface AlbumDetalhe {
  album: AlbumCatalogo;
  generos: string[];
  musicas: MusicaAlbum[];
}

export interface MusicaAlbum {
  idMusica: number;
  titulo: string;
  duracaoSegundos: number;
  generos: string[];
  curtida: boolean;
}
