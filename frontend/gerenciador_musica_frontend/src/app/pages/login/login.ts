import { Component } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})

export class Login {
  loginForm = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.email
      ]
    }),

  senha: new FormControl('', {
    nonNullable: true,
    validators: [
      Validators.required,
      Validators.minLength(6)
      ]
    })
  });

get email() {
  return this.loginForm.controls.email;
  }

get senha() {
  return this.loginForm.controls.senha;
  }


constructor(
  private authService: AuthService,
  private router: Router
  ) {}

entrar(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    
    const credenciais = this.loginForm.getRawValue(); 

    this.authService.login(credenciais).subscribe({
      
      next: (resposta) => {
        console.log('Login feito com sucesso!', resposta);
        this.router.navigate(['/']); // Muda de página
      },
      
      error: (erro) => {
        console.error('Erro no login', erro);
        alert('E-mail ou senha incorretos. Tente novamente!');
      }
    });
  }
}


