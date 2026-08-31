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
  const reviewsUrl = 'http://localhost:8080/api/reviews/musicas/1?page=0';

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

  function paginaVazia() {
    return { itens: [], paginaAtual: 0, tamanhoPagina: 20, totalItens: 0, totalPaginas: 0 };
  }

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(musicaUrl).flush(musicaDeExemplo());
    httpMock.expectOne(reviewsUrl).flush(paginaVazia());

    expect(component).toBeTruthy();
  });

  it('deve carregar os detalhes da música pelo id da rota', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(musicaUrl).flush(musicaDeExemplo());
    httpMock.expectOne(reviewsUrl).flush(paginaVazia());

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
    httpMock.expectOne(reviewsUrl).flush(paginaVazia());

    expect(component.mensagemErro()).toBe('Música não encontrada.');
  });

  it('deve exibir o player quando a música possuir vídeo', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(musicaUrl).flush({
      ...musicaDeExemplo(),
      youtubeVideoId: 'dQw4w9WgXcQ'
    });
    httpMock.expectOne(reviewsUrl).flush(paginaVazia());
    fixture.detectChanges();

    const iframe = fixture.nativeElement
      .querySelector('iframe') as HTMLIFrameElement | null;

    expect(iframe).not.toBeNull();
    expect(iframe?.src)
      .toBe('https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ');
    expect(fixture.nativeElement.textContent).toContain('Ouvir música');
  });

  it('não deve exibir o player sem um vídeo associado', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(musicaUrl).flush(musicaDeExemplo());
    httpMock.expectOne(reviewsUrl).flush(paginaVazia());
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('iframe')).toBeNull();
  });

  it('deve mostrar "Avaliar" quando o usuário ainda não tem review', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(musicaUrl).flush(musicaDeExemplo());
    httpMock.expectOne(reviewsUrl).flush(paginaVazia());
    fixture.detectChanges();

    const link = fixture.nativeElement.querySelector('.acao-avaliar');
    expect(link.textContent).toContain('Avaliar');
    expect(link.getAttribute('href')).toBe(
      '/reviews/nova?tipo=MUSICA&id=1&titulo=Bohemian%20Rhapsody&artista=Queen'
    );
  });

  it('deve linkar para a review existente quando o usuário já avaliou', () => {
    configurarTestBed();
    fixture.detectChanges();

    httpMock.expectOne(musicaUrl).flush(musicaDeExemplo());
    httpMock.expectOne(reviewsUrl).flush({
      itens: [{
        idReview: 7,
        autor: { id: 1, nome: 'Você' },
        alvo: { tipo: 'MUSICA', id: 1, titulo: 'Bohemian Rhapsody', artista: 'Queen', capaUrl: null },
        nota: 5,
        texto: null,
        criadaEm: '2026-01-01T00:00:00Z',
        atualizadaEm: '2026-01-01T00:00:00Z',
        minhaReview: true
      }],
      paginaAtual: 0,
      tamanhoPagina: 20,
      totalItens: 1,
      totalPaginas: 1
    });
    fixture.detectChanges();

    const link = fixture.nativeElement.querySelector('.acao-avaliar');
    expect(link.textContent).toContain('Ver minha review');
    expect(link.getAttribute('href')).toBe('/reviews/7');
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
