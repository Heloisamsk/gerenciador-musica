export interface MusicaResponse {
  id: number;
  titulo: string;
  duracao: number;
  anoLancamento: number;
  genero: GeneroResumo;
  artista: ArtistaResumo;
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
