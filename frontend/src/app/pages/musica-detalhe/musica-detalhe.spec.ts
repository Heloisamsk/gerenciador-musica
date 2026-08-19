import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';

import { MusicaDetalhe } from './musica-detalhe';

describe('MusicaDetalhe', () => {
  let component: MusicaDetalhe;
  let fixture: ComponentFixture<MusicaDetalhe>;
  let httpMock: HttpTestingController;

  const musicaUrl = 'http://localhost:8080/api/musicas/1';

  function configurarTestBed() {
    TestBed.configureTestingModule({
      imports: [MusicaDetalhe],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: convertToParamMap({ id: '1' }) },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MusicaDetalhe);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  }

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(musicaUrl).flush(musicaDeExemplo());

    expect(component).toBeTruthy();
  });

  it('deve carregar os detalhes da música pelo id da rota', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(musicaUrl).flush(musicaDeExemplo());

    expect(component.musica()?.titulo).toBe('Bohemian Rhapsody');
    expect(component.musica()?.letra).toBe('Is this the real life?');
    expect(component.carregando()).toBe(false);
  });

  it('deve mostrar mensagem específica quando a música não é encontrada (404)', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock
      .expectOne(musicaUrl)
      .flush({ message: 'erro' }, { status: 404, statusText: 'Not Found' });

    expect(component.mensagemErro()).toBe('Música não encontrada.');
  });

  function musicaDeExemplo() {
    return {
      id: 1,
      titulo: 'Bohemian Rhapsody',
      letra: 'Is this the real life?',
      duracaoSegundos: 354,
      anoLancamento: 1975,
      artistaPrincipal: { id: 1, nome: 'Queen' },
      album: { id: 1, titulo: 'A Night at the Opera', anoLancamento: 1975, capaUrl: null },
      artistasParticipantes: [],
      generos: [{ id: 1, nome: 'Rock' }],
    };
  }
});
