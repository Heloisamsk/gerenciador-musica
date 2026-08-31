import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AdminUsuarioService } from '../../services/admin-usuario';
import { UsuarioListagem } from '../../models/usuario-listagem';

@Component({
  selector: 'app-admin-usuarios',
  imports: [RouterLink],
  templateUrl: './admin-usuarios.html',
  styleUrl: './admin-usuarios.css'
})
export class AdminUsuarios implements OnInit {

  usuarios: UsuarioListagem[] = [];
  carregando = false;
  mensagemErro = '';

  constructor(
    private readonly adminUsuarioService: AdminUsuarioService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.carregarUsuarios();
  }

  carregarUsuarios(): void {
    if (this.carregando) {
      return;
    }

    this.carregando = true;
    this.mensagemErro = '';

    console.log('Iniciando carregamento dos usuários.');

    this.adminUsuarioService
      .listar()
      .pipe(
        finalize(() => {
          this.carregando = false;

          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: (usuarios) => {
          console.log('Usuários recebidos:', usuarios);
          this.usuarios = usuarios;
        },

        error: (erro: HttpErrorResponse) => {
          console.error('Erro ao carregar usuários:', erro);

          this.usuarios = [];

          if (erro.status === 401) {
            this.mensagemErro =
              'Sua sessão expirou ou não é válida. Faça login novamente.';
          } else if (erro.status === 403) {
            this.mensagemErro =
              'Você não possui permissão para visualizar os usuários.';
          } else if (erro.status === 0) {
            this.mensagemErro =
              'Não foi possível conectar ao backend.';
          } else {
            this.mensagemErro =
              'Não foi possível carregar os usuários.';
          }
        }
      });
  }
}
