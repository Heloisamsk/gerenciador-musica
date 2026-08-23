import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import type { ArtistaResponse } from '../../../../models/ArtistaResponse';
import { SeletorParticipantes } from './seletor-participantes';

describe('SeletorParticipantes', () => {
  let fixture: ComponentFixture<SeletorParticipantes>;
  let component: SeletorParticipantes;

  const artistas: ArtistaResponse[] = [
    {
      idArtista: 1,
      nome: 'ADÉLA',
      nomeCompleto: 'Adéla Jergová',
      descricao: 'Descrição de teste.',
      fotoPerfilUrl: null
    },
    {
      idArtista: 2,
      nome: 'Artista Dois',
      nomeCompleto: 'Nome Completo Dois',
      descricao: 'Descrição de teste.',
      fotoPerfilUrl: null
    },
    {
      idArtista: 3,
      nome: 'Artista Três',
      nomeCompleto: 'Nome Completo Três',
      descricao: 'Descrição de teste.',
      fotoPerfilUrl: null
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeletorParticipantes]
    }).compileComponents();

    fixture = TestBed.createComponent(SeletorParticipantes);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('artistas', artistas);
    fixture.componentRef.setInput('participantesIds', []);
    fixture.detectChanges();
  });

  function pesquisar(valor: string): void {
    const campo = fixture.nativeElement.querySelector(
      '#artistasParticipantesIds'
    ) as HTMLInputElement;
    campo.value = valor;
    campo.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  it('deve pesquisar ignorando acentos e sugerir por nome artístico', () => {
    pesquisar('adela');

    expect(component.sugestoes()).toEqual([artistas[0]]);
    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Adéla Jergová');
  });

  it('deve sugerir também pelo nome completo', () => {
    pesquisar('completo dois');

    expect(component.sugestoes()).toEqual([artistas[1]]);
  });

  it('deve adicionar a sugestão e limpar a busca', () => {
    const alterar = vi.fn();
    component.participantesIdsChange.subscribe(alterar);
    pesquisar('artista dois');

    (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>('.sugestao-participante')
      ?.click();

    expect(alterar).toHaveBeenCalledWith([2]);
    expect(component.busca()).toBe('');
  });

  it('deve exibir o chip selecionado e permitir removê-lo', () => {
    const alterar = vi.fn();
    component.participantesIdsChange.subscribe(alterar);
    fixture.componentRef.setInput('participantesIds', [2, 3]);
    fixture.detectChanges();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelectorAll('.participante-chip')).toHaveLength(2);

    elemento.querySelector<HTMLButtonElement>(
      'button[aria-label="Remover Artista Dois dos participantes"]'
    )?.click();

    expect(alterar).toHaveBeenCalledWith([3]);
  });

  it('deve adicionar a primeira sugestão com Enter e remover com Backspace', () => {
    const alterar = vi.fn();
    component.participantesIdsChange.subscribe(alterar);
    pesquisar('artista dois');

    component.tratarTecla(new KeyboardEvent('keydown', { key: 'Enter' }));
    expect(alterar).toHaveBeenCalledWith([2]);

    fixture.componentRef.setInput('participantesIds', [2]);
    component.busca.set('');
    component.tratarTecla(new KeyboardEvent('keydown', { key: 'Backspace' }));

    expect(alterar).toHaveBeenLastCalledWith([]);
  });

  it('deve impedir alterações quando estiver desabilitado', () => {
    const alterar = vi.fn();
    component.participantesIdsChange.subscribe(alterar);
    fixture.componentRef.setInput('participantesIds', [2]);
    fixture.componentRef.setInput('desabilitado', true);
    fixture.detectChanges();

    component.adicionar(artistas[0]);
    component.remover(2);
    pesquisar('adela');

    expect(alterar).not.toHaveBeenCalled();
    expect(component.sugestoes()).toEqual([]);
  });

  it('deve informar quando não houver sugestões e limpar com Escape', () => {
    pesquisar('nome inexistente');

    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Nenhum artista encontrado.');

    component.tratarTecla(new KeyboardEvent('keydown', { key: 'Escape' }));

    expect(component.busca()).toBe('');
  });
});
