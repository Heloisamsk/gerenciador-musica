import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
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

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  cadastrar(): void {
    if (this.cadastroForm.invalid) {
      this.cadastroForm.markAllAsTouched();
      return;
    }
    const dadosCadastro = this.cadastroForm.getRawValue();

    this.authService.cadastrar(dadosCadastro).subscribe({
      next: (resposta) => {
        console.log('Cadastro realizado com sucesso!', resposta);
        alert('Cadastro realizado com sucesso!');
        this.router.navigate(['/login']);
      },
      error: (erro) => {
        console.error('Erro no cadastro:', erro);

        if (erro.status === 409 || (erro.error && typeof erro.error === 'string' && erro.error.toLowerCase().includes('email'))) {
          alert('Este e-mail já está cadastrado. Tente utilizar outro.');
        }
        else if (erro.status === 400 && erro.error) {
          const mensagemErro = erro.error.message || 'Dados inválidos. Verifique as informações digitadas.';
          alert(`Erro de validação: ${mensagemErro}`);
        }
        else {
          alert('Ocorreu um erro ao realizar o cadastro. Tente novamente mais tarde.');
        }
      }
    });
  }
}
