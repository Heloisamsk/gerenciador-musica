import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAutenticado()) {
    return router.createUrlTree(['/login']);
  }

    const expectedRole = route.data['expectedRole'];

  if (
    expectedRole &&
    authService.getRole() !== expectedRole
  ) {
    alert(
      'Acesso negado: seu perfil não possui essa permissão.'
    );

    return router.createUrlTree(['/home']);
  }

    return true;
};

