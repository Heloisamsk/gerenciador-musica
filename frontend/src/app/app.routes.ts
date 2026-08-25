import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Cadastro } from './pages/cadastro/cadastro';
import { Home } from './pages/home/home';
import { authGuard } from './guards/auth-guard';
import { Playlists } from './pages/playlists/playlists';
import { PlaylistDetalhe } from './pages/playlist-detalhe/playlist-detalhe';
import { PlaylistNova } from './pages/playlist-nova/playlist-nova';
import { Catalogo } from './pages/catalogo/catalogo';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: Login
  },
  {
    path: 'cadastro',
    component: Cadastro
  },
  {
    path: 'home',
    component: Home,
    canActivate: [authGuard]
  },
  {
    path: 'admin/painel',
    loadComponent: () =>
      import('./pages/admin-painel/admin-painel')
        .then(modulo => modulo.AdminPainel),
    canActivate: [authGuard],
    data: {
      expectedRole: 'ADMIN'
    }
  },
  {
    path: 'admin',
    component: Home,
    canActivate: [authGuard],
    data: {
      expectedRole: 'ADMIN'
    }
  },
  {
  path: 'admin/banco/usuarios',
  loadComponent: () =>
    import('./pages/admin-usuarios/admin-usuarios')
      .then(modulo => modulo.AdminUsuarios),
  canActivate: [authGuard],
  data: {
    expectedRole: 'ADMIN'
  }
  },
 {
  path: 'admin/banco/musicas',
  loadComponent: () =>
    import('./pages/admin-musicas/admin-musicas')
      .then(modulo => modulo.AdminMusicas),
  canActivate: [authGuard],
  data: {
    expectedRole: 'ADMIN'
  }
  },
  {
  path: 'admin/banco/musicas/nova',
  loadComponent: () =>
    import('./pages/admin-musica-nova/admin-musica-nova')
      .then(modulo => modulo.AdminMusicaNova),
  canActivate: [authGuard],
  data: {
    expectedRole: 'ADMIN'
  }
  },
  {
    path: 'admin/banco/musicas/:id/editar',
    loadComponent: () =>
      import(
        './pages/admin-musicas/editar-musica/editar-musica'
      ).then(modulo => modulo.EditarMusica),
    canActivate: [authGuard],
    data: {
      expectedRole: 'ADMIN'
    }
  },
  {
    path: 'admin/banco/artistas',
    loadComponent: () =>
      import('./pages/admin-artistas/admin-artistas')
        .then(modulo => modulo.AdminArtistas),
    canActivate: [authGuard],
    data: {
      expectedRole: 'ADMIN'
    }
  },
  {
    path: 'admin/banco/artistas/novo',
    loadComponent: () =>
      import(
        './pages/admin-artistas/cadastro-artista/cadastro-artista'
      ).then(modulo => modulo.CadastroArtista),
    canActivate: [authGuard],
    data: {
      expectedRole: 'ADMIN'
    }
  },
  {
    path: 'admin/banco/artistas/:id/editar',
    loadComponent: () =>
      import(
        './pages/admin-artistas/editar-artista/editar-artista'
      ).then(modulo => modulo.EditarArtista),
    canActivate: [authGuard],
    data: {
      expectedRole: 'ADMIN'
    }
  },
  {
    path: 'admin/banco/albuns',
    loadComponent: () =>
      import('./pages/admin-albuns/admin-albuns')
        .then(modulo => modulo.AdminAlbuns),
    canActivate: [authGuard],
    data: {
      expectedRole: 'ADMIN'
    }
  },
  {
    path: 'admin/banco/albuns/novo',
    loadComponent: () =>
      import(
        './pages/admin-albuns/cadastro-album/cadastro-album'
      ).then(modulo => modulo.CadastroAlbum),
    canActivate: [authGuard],
    data: {
      expectedRole: 'ADMIN'
    }
  },
  {
    path: 'admin/banco/albuns/:id/editar',
    loadComponent: () =>
      import(
        './pages/admin-albuns/editar-album/editar-album'
      ).then(modulo => modulo.EditarAlbum),
    canActivate: [authGuard],
    data: {
      expectedRole: 'ADMIN'
    }
  },
  {
  path: 'playlists',
  component: Playlists,
  canActivate: [authGuard]
},
{
  path: 'playlists/nova',
  component: PlaylistNova,
  canActivate: [authGuard]
},
{
  path: 'playlists/:id',
  component: PlaylistDetalhe,
  canActivate: [authGuard]
},

{
  path: 'playlists/:id/catalogo',
  component: Catalogo,
  canActivate: [authGuard]
},

{
  path: 'musicas',
  loadComponent: () =>
    import('./pages/musicas/musicas')
      .then(modulo => modulo.Musicas),
  canActivate: [authGuard]
},

{
  path: 'musicas/:id',
  loadComponent: () =>
    import('./pages/musica-detalhe/musica-detalhe')
      .then(modulo => modulo.MusicaDetalhe),
  canActivate: [authGuard]
},

{
  path: 'artistas/:id',
  loadComponent: () =>
    import('./pages/artista-detalhe/artista-detalhe')
      .then(modulo => modulo.ArtistaDetalhePage),
  canActivate: [authGuard]
},

{
  path: 'albuns/:id',
  loadComponent: () =>
    import('./pages/album-detalhe/album-detalhe')
      .then(modulo => modulo.AlbumDetalhePage),
  canActivate: [authGuard]
},

{
  path: '**',
  redirectTo: 'login'
}

];
