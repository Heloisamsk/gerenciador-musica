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
  selector: 'app-cadastro',
  imports: [ReactiveFormsModule],
  templateUrl: './cadastro.html',
  styleUrl: './cadastro.css',
})
export class Cadastro {

  cadastroForm = new FormGroup({
    nome: new FormControl('', {
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
    })
  });

  get nome() {
    return this.cadastroForm.controls.nome;
  }

  get email() {
    return this.cadastroForm.controls.email;
  }

  get senha() {
    return this.cadastroForm.controls.senha;
  }

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  cadastrar(): void {
    if (this.cadastroForm.invalid) {
      this.cadastroForm.markAllAsTouched();
      return;
    }

    const dadosCadastro =
      this.cadastroForm.getRawValue();

    this.authService.cadastrar(dadosCadastro).subscribe({
      next: () => {
        alert('Cadastro realizado com sucesso!');
        this.router.navigate(['/login']);
      },

      error: (erro) => {
        console.error('Erro no cadastro:', erro);

        if (erro.status === 409) {
          const mensagem =
            erro.error?.message ??
            'Este e-mail já está cadastrado.';

          alert(mensagem);
        } else if (erro.status === 400) {
          const mensagem =
            erro.error?.message ??
            'Dados inválidos. Verifique as informações digitadas.';

          alert(`Erro de validação: ${mensagem}`);
        } else {
          alert(
            `Erro ao realizar o cadastro. Status: ${erro.status}`
          );
        }
      }
    });
  }
}
