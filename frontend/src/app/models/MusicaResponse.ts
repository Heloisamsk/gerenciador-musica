export interface MusicaResponse {
  id: number;
  titulo: string;
  duracaoSegundos: number;
  anoLancamento: number;
  generos: GeneroResumo[];
  artistaPrincipal: ArtistaResumo;
  album: AlbumResumo;
}

export interface GeneroResumo {
  id: number;
  nome: string;
}

export interface ArtistaResumo {
  id: number;
  nome: string;
}

export interface AlbumResumo {
  id: number;
  titulo: string;
}
