import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AdminMusicas } from './admin-musicas';
import { MusicaListagem } from '../../models/MusicaListagem';

describe('AdminMusicas', () => {
  let component: AdminMusicas;
  let fixture: ComponentFixture<AdminMusicas>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/musicas';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminMusicas],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminMusicas);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function musicaDeExemplo(generos: { id: number; nome: string }[]): MusicaListagem {
    return {
      id: 1,
      titulo: 'Bohemian Rhapsody',
      duracaoSegundos: 354,
      anoLancamento: 1975,
      artistaPrincipal: { id: 1, nome: 'Queen' },
      album: { id: 1, titulo: 'A Night at the Opera', anoLancamento: 1975, capaUrl: null },
      generos,
    };
  }

  it('deve carregar a listagem de músicas ao iniciar', () => {
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}?size=100`).flush({
      itens: [musicaDeExemplo([{ id: 1, nome: 'Rock' }])],
      paginaAtual: 0,
      tamanhoPagina: 100,
      totalItens: 1,
      totalPaginas: 1,
    });

    expect(component.musicas()).toHaveLength(1);
    expect(component.musicas()[0].titulo).toBe('Bohemian Rhapsody');
    expect(component.carregando()).toBe(false);
  });

  it('deve mostrar mensagem de erro quando a busca falha', () => {
    fixture.detectChanges();

    httpMock
      .expectOne(`${apiUrl}?size=100`)
      .flush({ message: 'erro' }, { status: 500, statusText: 'Internal Server Error' });

    expect(component.mensagemErro()).toBeTruthy();
    expect(component.carregando()).toBe(false);
  });

  it('generosTexto deve juntar os nomes dos gêneros separados por vírgula', () => {
    const musica = musicaDeExemplo([
      { id: 1, nome: 'Rock' },
      { id: 2, nome: 'Pop' },
    ]);

    expect(component.generosTexto(musica)).toBe('Rock, Pop');
  });

  it('generosTexto deve retornar "-" quando a música não tem gênero', () => {
    const musica = musicaDeExemplo([]);

    expect(component.generosTexto(musica)).toBe('-');
  });
});
