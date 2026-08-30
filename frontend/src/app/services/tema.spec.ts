import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { TemaService } from './tema';

describe('TemaService', () => {
  afterEach(() => {
    document.documentElement.removeAttribute('data-tema');
    document.documentElement.style.colorScheme = '';
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('deve persistir e aplicar o tema escolhido', () => {
    localStorage.setItem('crotchet-tema', 'claro');
    const service = TestBed.inject(TemaService);

    expect(service.tema()).toBe('claro');
    expect(document.documentElement.dataset['tema']).toBe('claro');

    service.alternar();

    expect(service.tema()).toBe('escuro');
    expect(localStorage.getItem('crotchet-tema')).toBe('escuro');
  });
});
