import { MusicaResumo } from './MusicaResumo';

export interface PlaylistResponse {
  id: number;
  nome: string;
  descricao: string;
  musicas: MusicaResumo[];
}
