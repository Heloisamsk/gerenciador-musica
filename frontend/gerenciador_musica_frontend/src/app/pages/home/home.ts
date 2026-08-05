import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-home',
  imports: [],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  constructor(private authService: AuthService, private router: Router) {}

  fazerLogout(): void {
    // 1. Limpa o token e a role do navegador
    this.authService.logout();
    
    // 2. Manda o usuário de volta para a tela de login
    this.router.navigate(['/login']);
  }
}
