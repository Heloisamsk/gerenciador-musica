import { Component, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../services/auth';
import { AlbumBackdrop } from '../../shared/album-backdrop/album-backdrop';
import { VinylMark } from '../../shared/vinyl-mark/vinyl-mark';

@Component({
  selector: 'app-cadastro',
  imports: [ReactiveFormsModule, VinylMark, AlbumBackdrop],
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

  cadastrando = signal(false);

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  cadastrar(): void {
    if (this.cadastroForm.invalid || this.cadastrando()) {
      this.cadastroForm.markAllAsTouched();
      return;
    }

    const dadosCadastro = this.cadastroForm.getRawValue();
    this.cadastrando.set(true);

    this.authService.cadastrar(dadosCadastro)
      .pipe(finalize(() => this.cadastrando.set(false)))
      .subscribe({
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
