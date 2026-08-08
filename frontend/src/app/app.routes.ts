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
    path: '**',
    redirectTo: 'login'
  }
];
