import { ArtistaResumo, AlbumResumo, GeneroResumo } from './MusicaResponse';

export interface MusicaListagem {
  id: number;
  titulo: string;
  duracaoSegundos: number;
  anoLancamento: number;
  artistaPrincipal: ArtistaResumo;
  album: AlbumResumo | null;
  artistasParticipantes: ArtistaResumo[];
  generos: GeneroResumo[];
}
