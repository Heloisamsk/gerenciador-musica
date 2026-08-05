import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Cadastro } from './pages/cadastro/cadastro';
import { Home } from './pages/home/home';

export const routes: Routes = [
    {
      path: 'login',
      component: Login,
      pathMatch: 'full'
    },
    {
      path: 'cadastro',
      component: Cadastro
    },
    {
      path: 'home',
      component: Home
    }
  ];
