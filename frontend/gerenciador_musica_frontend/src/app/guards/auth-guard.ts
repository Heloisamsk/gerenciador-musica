import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAutenticado()) {
    router.navigate(['/login']);
    return false;
  } else {

    const expectedRole = route.data['expectedRole'];

    if (expectedRole) {
      const userRole = localStorage.getItem('role');

      if (userRole !== expectedRole) {
        alert('Acesso negado: você não tem permissão de Administrador!');
        router.navigate(['/login']);
        return false;
      }
    }

    return true;
  }
};

