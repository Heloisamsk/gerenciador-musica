import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './perfil.html',
  styleUrls: ['./perfil.css']
})
export class Perfil implements OnInit {
  nome = signal('Usuário');
  email = signal('');
  role = signal('');

  constructor(private readonly authService: AuthService) {}

  ngOnInit(): void {
    if (typeof window !== 'undefined') {
      this.nome.set(localStorage.getItem('nome') || 'Usuário');
      this.email.set(localStorage.getItem('email') || 'email@dominio.com');
      this.role.set(localStorage.getItem('role') || 'USER');
    }
  }
}
