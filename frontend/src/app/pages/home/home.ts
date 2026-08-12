import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  constructor(private authService: AuthService, private router: Router) {}

  get isAdmin(): boolean {
    return this.authService.getRole() === 'ADMIN';
  }

fazerLogout(): void {
  this.authService.logout().subscribe({
    next: () => {
      void this.router.navigate(['/login']);
    },

    error: (erro) => {
      console.error(
        'Erro ao comunicar o logout ao backend:',
        erro
      );

      void this.router.navigate(['/login']);
    }
  });
  }
}
