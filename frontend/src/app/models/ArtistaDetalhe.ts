export interface ArtistaDetalhe {
  artista: ArtistaCatalogoResumo;
  albuns: AlbumCatalogo[];
  musicas: MusicaCatalogo[];
}

export interface ArtistaCatalogoResumo {
  idArtista: number;
  nome: string;
  nomeCompleto: string;
  descricao: string;
  fotoPerfilUrl: string | null;
  totalAlbuns: number;
  totalMusicasPrincipais: number;
  totalParticipacoes: number;
  duracaoTotalSegundos: number;
}

export interface AlbumCatalogo {
  idAlbum: number;
  idArtista: number;
  nomeArtista: string;
  titulo: string;
  anoLancamento: number;
  capaUrl: string | null;
  totalMusicas: number;
  duracaoTotalSegundos: number;
  curtida: boolean;
}

export interface MusicaCatalogo {
  idMusica: number;
  titulo: string;
  duracaoSegundos: number;
  anoLancamento: number;
  idArtistaPrincipal: number;
  nomeArtistaPrincipal: string;
  idAlbum: number | null;
  tituloAlbum: string | null;
  capaUrl: string | null;
  generos: string[];
  papelArtista: 'PRINCIPAL' | 'PARTICIPANTE';
}
