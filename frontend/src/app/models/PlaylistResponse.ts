import { MusicaResumo } from './MusicaResumo';

export interface PlaylistResponse {
  id: number;
  nome: string;
  descricao: string;
  capaUrl: string | null;
  musicas: MusicaResumo[];
  especial: boolean;
}
