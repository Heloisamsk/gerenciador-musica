export type TipoAlvoReview = 'MUSICA' | 'ALBUM';

export interface ReviewAutor {
  id: number;
  nome: string;
}

export interface ReviewAlvo {
  tipo: TipoAlvoReview;
  id: number;
  titulo: string;
  artista: string | null;
  capaUrl: string | null;
}

export interface Review {
  idReview: number;
  autor: ReviewAutor;
  alvo: ReviewAlvo;
  nota: number;
  texto: string | null;
  criadaEm: string;
  atualizadaEm: string;
  minhaReview: boolean;
}

export interface ReviewRequest {
  idMusica: number | null;
  idAlbum: number | null;
  nota: number;
  texto: string | null;
}

export interface ReviewAtualizacaoRequest {
  nota: number;
  texto: string | null;
}
