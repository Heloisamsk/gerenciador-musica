export interface MusicaRequest {
  titulo: string;
  letra?: string | null;
  duracaoSegundos: number;
  anoLancamento: number;
  artistaPrincipalId: number;
  artistasParticipantesIds: number[];
  albumId: number | null;
  generos: string[];
  youtubeUrl?: string | null;
}
