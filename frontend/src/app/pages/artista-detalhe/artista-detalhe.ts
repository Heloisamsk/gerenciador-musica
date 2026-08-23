import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import type { ArtistaDetalhe } from '../../models/ArtistaDetalhe';
import { CatalogoService } from '../../services/catalogo';
import { formatarDuracao } from '../../shared/formatar-duracao';

@Component({
  selector: 'app-artista-detalhe',
  imports: [RouterLink],
  templateUrl: './artista-detalhe.html',
  styleUrl: './artista-detalhe.css'
})
export class ArtistaDetalhePage implements OnInit {

  readonly detalhes = signal<ArtistaDetalhe | null>(null);
  readonly carregando = signal(false);
  readonly mensagemErro = signal('');
  readonly idArtista = signal<number | null>(null);
  readonly formatarDuracao = formatarDuracao;

  constructor(
    private readonly catalogoService: CatalogoService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isInteger(id) || id <= 0) {
      this.mensagemErro.set('O identificador do artista é inválido.');
      return;
    }

    this.idArtista.set(id);
    this.carregarDetalhes();
  }

  tentarNovamente(): void {
    this.carregarDetalhes();
  }

  substituirImagem(
    evento: Event,
    imagemPadrao: string
  ): void {
    const imagem = evento.target as HTMLImageElement;

    imagem.onerror = null;
    imagem.src = imagemPadrao;
  }

  rotuloPapel(papel: 'PRINCIPAL' | 'PARTICIPANTE'): string {
    return papel === 'PARTICIPANTE'
      ? 'Participação'
      : 'Artista principal';
  }

  private carregarDetalhes(): void {
    const idArtista = this.idArtista();

    if (idArtista === null || this.carregando()) {
      return;
    }

    this.carregando.set(true);
    this.mensagemErro.set('');

    this.catalogoService
      .buscarDetalhesArtista(idArtista)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: detalhes => this.detalhes.set(detalhes),
        error: (erro: HttpErrorResponse) => {
          this.mensagemErro.set(this.mensagemParaErro(erro));
        }
      });
  }

  private mensagemParaErro(erro: HttpErrorResponse): string {
    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor. Tente novamente.';
      case 400:
        return 'O identificador do artista é inválido.';
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não tem permissão para consultar este artista.';
      case 404:
        return 'Artista não encontrado.';
      case 500:
        return 'Ocorreu um erro no servidor. Tente novamente mais tarde.';
      default:
        return 'Não foi possível carregar o artista. Tente novamente.';
    }
  }
}
