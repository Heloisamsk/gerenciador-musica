import type { AlbumResponse } from './AlbumResponse';
import type { ArtistaResponse } from './ArtistaResponse';
import type { MusicaListagem } from './MusicaListagem';

export interface UsuarioBusca {
  idUsuario: number;
  nome: string;
  username: string | null;
}

export interface BuscaResultado {
  musicas: MusicaListagem[];
  albuns: AlbumResponse[];
  artistas: ArtistaResponse[];
  usuarios: UsuarioBusca[];
}
