import { Component, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
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

  entrando = signal(false);

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  entrar(): void {
    if (this.loginForm.invalid || this.entrando()) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const credenciais = this.loginForm.getRawValue();
    this.entrando.set(true);

    this.authService.login(credenciais)
      .pipe(finalize(() => this.entrando.set(false)))
      .subscribe({
        next: (resposta) => {
          console.log(
            'Login feito com sucesso!',
            resposta
          );

          void this.router.navigate(['/home']);
        },

        error: (erro) => {
          console.error('Erro no login', erro);

          alert(
            'E-mail ou senha incorretos. Tente novamente!'
          );
        }
      });
  }
}
