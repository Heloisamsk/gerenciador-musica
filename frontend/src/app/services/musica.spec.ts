import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { MusicaService } from './musica';
import { MusicaListagem } from '../models/MusicaListagem';
import { MusicaResponse } from '../models/MusicaResponse';
import { PaginaResponse } from '../models/PaginaResponse';

describe('MusicaService', () => {
  let service: MusicaService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/musicas';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        MusicaService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(MusicaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('deve pesquisar sem enviar nenhum parâmetro quando nada é informado', () => {
    service.pesquisar().subscribe();

    const requisicao = httpMock.expectOne(apiUrl);

    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.params.keys()).toHaveLength(0);

    requisicao.flush(paginaDeExemplo());
  });

  it('deve montar os query params apenas com os filtros preenchidos', () => {
    service.pesquisar({ titulo: 'amor', artistaId: 5 }, 1, 10, 'anoLancamento,desc').subscribe();

    const requisicao = httpMock.expectOne(
      `${apiUrl}?titulo=amor&artistaId=5&page=1&size=10&sort=anoLancamento,desc`
    );

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush(paginaDeExemplo());
  });

  it('não deve enviar um filtro de título composto apenas por espaços', () => {
    service.pesquisar({ titulo: '   ' }).subscribe();

    const requisicao = httpMock.expectOne(apiUrl);

    expect(requisicao.request.params.has('titulo')).toBe(false);

    requisicao.flush(paginaDeExemplo());
  });

  it('deve devolver a página de resultados recebida do backend', () => {
    let resultado: PaginaResponse<MusicaListagem> | undefined;

    service.pesquisar().subscribe((pagina) => (resultado = pagina));

    httpMock.expectOne(apiUrl).flush(paginaDeExemplo());

    expect(resultado?.itens).toHaveLength(1);
    expect(resultado?.itens[0].titulo).toBe('Bohemian Rhapsody');
  });

  it('deve buscar os detalhes de uma música por id', () => {
    let resultado: MusicaResponse | undefined;

    service.buscarPorId(1).subscribe((musica) => (resultado = musica));

    const requisicao = httpMock.expectOne(`${apiUrl}/1`);
    expect(requisicao.request.method).toBe('GET');

    requisicao.flush(musicaDetalheDeExemplo());

    expect(resultado?.letra).toBe('Is this the real life?');
    expect(resultado?.artistasParticipantes).toHaveLength(0);
  });

  function paginaDeExemplo(): PaginaResponse<MusicaListagem> {
    return {
      itens: [
        {
          id: 1,
          titulo: 'Bohemian Rhapsody',
          duracaoSegundos: 354,
          anoLancamento: 1975,
          artistaPrincipal: { id: 1, nome: 'Queen' },
          album: { id: 1, titulo: 'A Night at the Opera', anoLancamento: 1975, capaUrl: null },
          generos: [{ id: 1, nome: 'Rock' }],
        },
      ],
      paginaAtual: 0,
      tamanhoPagina: 20,
      totalItens: 1,
      totalPaginas: 1,
    };
  }

  function musicaDetalheDeExemplo(): MusicaResponse {
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
