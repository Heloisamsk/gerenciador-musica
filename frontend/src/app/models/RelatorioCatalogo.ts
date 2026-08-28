export interface ResumoCatalogo {
  totalArtistas: number;
  totalAlbuns: number;
  totalMusicas: number;
  totalParticipacoes: number;
  duracaoTotalSegundos: number;
}

export interface RelatorioArtista {
  idArtista: number;
  nome: string;
  totalAlbuns: number;
  totalMusicasPrincipais: number;
  totalParticipacoes: number;
  duracaoTotalSegundos: number;
}

export interface RelatorioAlbum {
  idAlbum: number;
  titulo: string;
  nomeArtista: string;
  anoLancamento: number;
  totalMusicas: number;
  duracaoTotalSegundos: number;
}

export interface RelatorioCatalogo {
  geradoEm: string;
  resumo: ResumoCatalogo;
  artistas: RelatorioArtista[];
  albuns: RelatorioAlbum[];
}

export type TipoRelatorio = 'ARTISTAS' | 'ALBUNS';
