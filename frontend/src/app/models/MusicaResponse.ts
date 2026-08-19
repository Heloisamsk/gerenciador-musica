export interface MusicaResponse {
  id: number;
  titulo: string;
  letra: string | null;
  duracaoSegundos: number;
  anoLancamento: number;
  artistaPrincipal: ArtistaResumo;
  album: AlbumResumo | null;
  artistasParticipantes: ArtistaResumo[];
  generos: GeneroResumo[];
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
  anoLancamento: number;
  capaUrl: string | null;
}
