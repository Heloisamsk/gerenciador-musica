import { DOCUMENT, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { RelatorioCatalogo, TipoRelatorio } from '../../models/RelatorioCatalogo';
import { AdminRelatorioService } from '../../services/admin-relatorio';

@Component({
  selector: 'app-admin-relatorios',
  imports: [DatePipe, RouterLink],
  templateUrl: './admin-relatorios.html',
  styleUrl: './admin-relatorios.css',
})
export class AdminRelatorios implements OnInit {
  private readonly relatorioService = inject(AdminRelatorioService);
  private readonly document = inject(DOCUMENT);

  readonly relatorio = signal<RelatorioCatalogo | null>(null);
  readonly carregando = signal(false);
  readonly exportando = signal<TipoRelatorio | null>(null);
  readonly mensagemErro = signal('');
  readonly mensagemErroExportacao = signal('');
  readonly abaAtiva = signal<TipoRelatorio>('ARTISTAS');
  readonly termoBusca = signal('');

  readonly artistasFiltrados = computed(() => {
    const artistas = this.relatorio()?.artistas ?? [];
    const termo = this.normalizar(this.termoBusca());

    return termo
      ? artistas.filter((artista) => this.normalizar(artista.nome).includes(termo))
      : artistas;
  });

  readonly albunsFiltrados = computed(() => {
    const albuns = this.relatorio()?.albuns ?? [];
    const termo = this.normalizar(this.termoBusca());

    return termo
      ? albuns.filter((album) =>
          this.normalizar(`${album.titulo} ${album.nomeArtista} ${album.anoLancamento}`).includes(
            termo,
          ),
        )
      : albuns;
  });

  ngOnInit(): void {
    this.carregarRelatorio();
  }

  carregarRelatorio(): void {
    if (this.carregando()) {
      return;
    }

    this.carregando.set(true);
    this.mensagemErro.set('');

    this.relatorioService
      .gerarCatalogo()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (relatorio) => this.relatorio.set(relatorio),
        error: (erro: HttpErrorResponse) => {
          this.relatorio.set(null);
          this.mensagemErro.set(this.mensagemParaErro(erro));
        },
      });
  }

  selecionarAba(tipo: TipoRelatorio): void {
    this.abaAtiva.set(tipo);
    this.termoBusca.set('');
    this.mensagemErroExportacao.set('');
  }

  atualizarBusca(evento: Event): void {
    this.termoBusca.set((evento.target as HTMLInputElement).value);
  }

  exportarCsv(tipo: TipoRelatorio): void {
    if (this.exportando() !== null) {
      return;
    }

    this.exportando.set(tipo);
    this.mensagemErroExportacao.set('');

    this.relatorioService
      .exportarCatalogo(tipo)
      .pipe(finalize(() => this.exportando.set(null)))
      .subscribe({
        next: (arquivo) => this.salvarArquivo(arquivo, tipo),
        error: (erro: HttpErrorResponse) => {
          this.mensagemErroExportacao.set(this.mensagemParaErroExportacao(erro));
        },
      });
  }

  formatarDuracao(segundosTotais: number): string {
    const horas = Math.floor(segundosTotais / 3600);
    const minutos = Math.floor((segundosTotais % 3600) / 60);

    if (horas > 0) {
      return `${horas}h ${minutos}min`;
    }

    return `${minutos}min`;
  }

  private salvarArquivo(arquivo: Blob, tipo: TipoRelatorio): void {
    const janela = this.document.defaultView;

    if (!janela?.URL.createObjectURL) {
      this.mensagemErroExportacao.set('O download não está disponível neste navegador.');
      return;
    }

    const url = janela.URL.createObjectURL(arquivo);
    const link = this.document.createElement('a');

    link.href = url;
    link.download = tipo === 'ARTISTAS' ? 'relatorio-artistas.csv' : 'relatorio-albuns.csv';
    this.document.body.appendChild(link);
    link.click();
    link.remove();
    janela.URL.revokeObjectURL(url);
  }

  private normalizar(valor: string): string {
    return valor
      .trim()
      .toLocaleLowerCase('pt-BR')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  private mensagemParaErro(erro: HttpErrorResponse): string {
    switch (erro.status) {
      case 0:
        return 'Não foi possível conectar ao servidor.';
      case 401:
        return 'Sua sessão expirou. Faça login novamente.';
      case 403:
        return 'Você não possui permissão para visualizar relatórios.';
      default:
        return 'Não foi possível gerar os relatórios.';
    }
  }

  private mensagemParaErroExportacao(erro: HttpErrorResponse): string {
    if (erro.status === 403) {
      return 'Você não possui permissão para exportar relatórios.';
    }

    return 'Não foi possível exportar o relatório em CSV.';
  }
}
