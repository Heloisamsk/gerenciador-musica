import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID, signal } from '@angular/core';

export type TemaAplicacao = 'claro' | 'escuro';

@Injectable({ providedIn: 'root' })
export class TemaService {
  readonly tema = signal<TemaAplicacao>('escuro');

  constructor(
    @Inject(DOCUMENT) private readonly documento: Document,
    @Inject(PLATFORM_ID) platformId: object
  ) {
    if (isPlatformBrowser(platformId)) {
      this.aplicar(this.obterTemaInicial(), false);
    }
  }

  alternar(): void {
    this.aplicar(this.tema() === 'escuro' ? 'claro' : 'escuro');
  }

  rotuloAcao(): string {
    return this.tema() === 'escuro'
      ? 'Ativar modo claro'
      : 'Ativar modo escuro';
  }

  private obterTemaInicial(): TemaAplicacao {
    try {
      const salvo = this.documento.defaultView?.localStorage
        .getItem('crotchet-tema');
      if (salvo === 'claro' || salvo === 'escuro') return salvo;

      return this.documento.defaultView
        ?.matchMedia('(prefers-color-scheme: light)').matches
        ? 'claro'
        : 'escuro';
    } catch {
      return 'escuro';
    }
  }

  private aplicar(tema: TemaAplicacao, persistir = true): void {
    this.tema.set(tema);
    this.documento.documentElement.dataset['tema'] = tema;
    this.documento.documentElement.style.colorScheme = tema === 'claro'
      ? 'light'
      : 'dark';

    if (!persistir) return;

    try {
      this.documento.defaultView?.localStorage.setItem('crotchet-tema', tema);
    } catch {
      // O tema continua ativo mesmo quando o navegador bloqueia o storage.
    }
  }
}
