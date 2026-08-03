import { Component } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

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

entrar(): void {
  if (this.loginForm.invalid) {
    this.loginForm.markAllAsTouched();
    return;
  }

  console.log(this.loginForm.getRawValue());
  }
}


