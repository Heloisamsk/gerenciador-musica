import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Cadastro } from './pages/cadastro/cadastro';
import { Home } from './pages/home/home';
import { authGuard } from './guards/auth-guard';

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
    path: '**',
    redirectTo: 'login'
  }

];
