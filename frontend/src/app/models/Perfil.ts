import type { Role } from '../services/auth';

export type TipoDestaquePerfil = 'ARTISTA' | 'MUSICA' | 'ALBUM';

export interface PerfilItem {
  tipo: TipoDestaquePerfil;
  id: number;
  titulo: string;
  subtitulo: string;
  imagemUrl: string | null;
}

export interface PerfilResponse {
  idUsuario: number;
  username: string | null;
  nome: string;
  dataCadastro: string;
  role: Role;
  fotoUrl: string | null;
  bannerUrl: string | null;
  biografia: string | null;
  fraseDestaque: string | null;
  tipoDestaquePrincipal: TipoDestaquePerfil | null;
  artistaDestaque: PerfilItem | null;
  musicaDestaque: PerfilItem | null;
  albumDestaque: PerfilItem | null;
  artistasFavoritos: PerfilItem[];
  albunsFavoritos: PerfilItem[];
  musicasFavoritas: PerfilItem[];
  totalMusicasAvaliadas: number;
  totalAlbunsAvaliadas: number;
  totalSeguidores?: number;
  totalSeguindo?: number;
  perfilDoUsuarioAutenticado?: boolean;
  seguindoPorUsuarioAutenticado?: boolean;
}

export interface AtualizarPerfilRequest {
  nome: string;
  username: string | null;
  fotoUrl: string | null;
  bannerUrl: string | null;
  biografia: string | null;
  fraseDestaque: string | null;
  idArtistaDestaque: number | null;
  idMusicaDestaque: number | null;
  idAlbumDestaque: number | null;
  tipoDestaquePrincipal: TipoDestaquePerfil | null;
  idsArtistasFavoritos: number[];
  idsAlbunsFavoritos: number[];
  idsMusicasFavoritas: number[];
}
