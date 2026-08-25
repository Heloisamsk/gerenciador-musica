import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { finalize, Observable } from 'rxjs';

interface MensagensDetalheCatalogo {
  idInvalido: string;
  acessoNegado: string;
  naoEncontrado: string;
  falhaCarregamento: string;
}

export abstract class DetalheCatalogoBase<T> {

  readonly detalhes = signal<T | null>(null);
  readonly carregando = signal(false);
  readonly mensagemErro = signal('');
  protected readonly idRecurso = signal<number | null>(null);

  protected constructor(
    private readonly buscarDetalhes: (id: number) => Observable<T>,
    private readonly mensagens: MensagensDetalheCatalogo
  ) {}

  tentarNovamente(): void {
    this.carregarDetalhes();
  }

  substituirImagem(evento: Event, imagemPadrao: string): void {
    const imagem = evento.target as HTMLImageElement;

    imagem.onerror = null;
    imagem.src = imagemPadrao;
  }

  protected inicializar(idRota: string | null): void {
    const id = Number(idRota);

    if (!Number.isInteger(id) || id <= 0) {
      this.mensagemErro.set(this.mensagens.idInvalido);
      return;
    }

    this.idRecurso.set(id);
    this.carregarDetalhes();
  }

  private carregarDetalhes(): void {
    const id = this.idRecurso();

    if (id === null || this.carregando()) {
      return;
    }

    this.carregando.set(true);
    this.mensagemErro.set('');

    this.buscarDetalhes(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: detalhes => this.detalhes.set(detalhes),
        error: (erro: HttpErrorResponse) => {
          this.mensagemErro.set(this.mensagemParaErro(erro));
        }
      });
  }

  private mensagemParaErro(erro: HttpErrorResponse): string {
    const mensagensPorStatus: Record<number, string> = {
      0: 'Não foi possível conectar ao servidor. Tente novamente.',
      400: this.mensagens.idInvalido,
      401: 'Sua sessão expirou. Faça login novamente.',
      403: this.mensagens.acessoNegado,
      404: this.mensagens.naoEncontrado,
      500: 'Ocorreu um erro no servidor. Tente novamente mais tarde.'
    };

    return mensagensPorStatus[erro.status]
      ?? this.mensagens.falhaCarregamento;
  }
}
