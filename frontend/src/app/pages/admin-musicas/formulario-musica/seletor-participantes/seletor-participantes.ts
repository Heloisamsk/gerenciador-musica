import {
  Component,
  computed,
  input,
  output,
  signal
} from '@angular/core';

import type { ArtistaResponse } from '../../../../models/ArtistaResponse';

@Component({
  selector: 'app-seletor-participantes',
  templateUrl: './seletor-participantes.html',
  styleUrl: './seletor-participantes.css'
})
export class SeletorParticipantes {

  readonly artistas = input<readonly ArtistaResponse[]>([]);
  readonly participantesIds = input<readonly number[]>([]);
  readonly desabilitado = input(false);

  readonly participantesIdsChange = output<number[]>();
  readonly busca = signal('');

  readonly participantesSelecionados = computed(() => {
    const artistasPorId = new Map(
      this.artistas().map(artista => [artista.idArtista, artista])
    );

    return this.participantesIds()
      .map(id => artistasPorId.get(id))
      .filter((artista): artista is ArtistaResponse => artista !== undefined);
  });

  readonly sugestoes = computed(() => {
    const termo = this.normalizarParaBusca(this.busca());

    if (termo.length === 0 || this.desabilitado()) {
      return [];
    }

    const selecionados = new Set(this.participantesIds());

    return this.artistas()
      .filter(artista => !selecionados.has(artista.idArtista))
      .filter(artista => {
        const nome = this.normalizarParaBusca(artista.nome);
        const nomeCompleto = this.normalizarParaBusca(
          artista.nomeCompleto
        );

        return nome.includes(termo) || nomeCompleto.includes(termo);
      })
      .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
      .slice(0, 6);
  });

  atualizarBusca(evento: Event): void {
    const campo = evento.target;

    if (campo instanceof HTMLInputElement) {
      this.busca.set(campo.value);
    }
  }

  adicionar(artista: ArtistaResponse): void {
    if (
      this.desabilitado()
      || this.participantesIds().includes(artista.idArtista)
    ) {
      return;
    }

    this.participantesIdsChange.emit([
      ...this.participantesIds(),
      artista.idArtista
    ]);
    this.busca.set('');
  }

  remover(idArtista: number): void {
    if (this.desabilitado()) {
      return;
    }

    this.participantesIdsChange.emit(
      this.participantesIds().filter(id => id !== idArtista)
    );
  }

  tratarTecla(evento: KeyboardEvent): void {
    if (evento.key === 'Escape') {
      this.busca.set('');
      return;
    }

    if (evento.key === 'Enter') {
      const primeiraSugestao = this.sugestoes()[0];

      if (primeiraSugestao) {
        evento.preventDefault();
        this.adicionar(primeiraSugestao);
      }

      return;
    }

    if (
      evento.key === 'Backspace'
      && this.busca().length === 0
    ) {
      const ids = this.participantesIds();
      const ultimoId = ids.at(-1);

      if (ultimoId !== undefined) {
        this.remover(ultimoId);
      }
    }
  }

  private normalizarParaBusca(valor: string): string {
    return valor
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLocaleLowerCase('pt-BR');
  }
}
