import { Component } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

@Component({
  selector: 'app-cadastro',
  imports: [ReactiveFormsModule],
  templateUrl: './cadastro.html',
  styleUrl: './cadastro.css',
})
export class Cadastro {
  cadastroForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),

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
    }),

    role: new FormControl('usuario', {
      nonNullable: true,
    })
  });

  get name() {
    return this.cadastroForm.controls.name;
  }

  get email() {
    return this.cadastroForm.controls.email;
  }

  get senha() {
    return this.cadastroForm.controls.senha;
  }

  get role() {
    return this.cadastroForm.controls.role;
  }

  cadastrar(): void {
  if (this.cadastroForm.invalid) {
    this.cadastroForm.markAllAsTouched();
    return;
  }

  console.log(this.cadastroForm.getRawValue());
  }
}
