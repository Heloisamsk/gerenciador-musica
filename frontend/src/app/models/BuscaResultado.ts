import type { AlbumResponse } from './AlbumResponse';
import type { ArtistaResponse } from './ArtistaResponse';
import type { MusicaListagem } from './MusicaListagem';

export interface BuscaResultado {
  musicas: MusicaListagem[];
  albuns: AlbumResponse[];
  artistas: ArtistaResponse[];
}
