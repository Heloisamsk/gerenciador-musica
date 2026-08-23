import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AdminMusicaService } from './admin-musica';
import { MusicaListagem } from '../models/MusicaListagem';
import { MusicaRequest } from '../models/MusicaRequest';
import { MusicaResponse } from '../models/MusicaResponse';

describe('AdminMusicaService', () => {
  let service: AdminMusicaService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/musicas';
  const apiAdminUrl =
    'http://localhost:8080/api/admin/musicas';

  function musicaDetalhadaDeExemplo(): MusicaResponse {
    return {
      id: 7,
      titulo: 'Música de teste',
      letra: 'Texto de teste.',
      duracaoSegundos: 210,
      anoLancamento: 2026,
      artistaPrincipal: {
        id: 2,
        nome: 'Artista principal'
      },
      album: {
        id: 3,
        titulo: 'Álbum de teste',
        anoLancamento: 2026,
        capaUrl: null
      },
      artistasParticipantes: [
        {
          id: 4,
          nome: 'Artista participante'
        }
      ],
      generos: [
        {
          id: 5,
          nome: 'Gênero de teste'
        }
      ]
    };
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AdminMusicaService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(AdminMusicaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('deve buscar a lista de músicas cadastradas', () => {
    service.listarMusicas().subscribe((musicas) => {
      expect(musicas).toHaveLength(1);
      expect(musicas[0].titulo).toBe('Bohemian Rhapsody');
    });

    const requisicao = httpMock.expectOne(`${apiUrl}?size=100`);

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush({
      itens: [
        {
          id: 1,
          titulo: 'Bohemian Rhapsody',
          duracaoSegundos: 354,
          anoLancamento: 1975,
          artistaPrincipal: { id: 1, nome: 'Queen' },
          album: { id: 1, titulo: 'A Night at the Opera', anoLancamento: 1975, capaUrl: null },
          artistasParticipantes: [],
          generos: [{ id: 1, nome: 'Rock' }],
        } as MusicaListagem,
      ],
      paginaAtual: 0,
      tamanhoPagina: 100,
      totalItens: 1,
      totalPaginas: 1,
    });
  });

  it('deve cadastrar música pelo endpoint administrativo', () => {
    const request: MusicaRequest = {
      titulo: 'Bohemian Rhapsody',
      duracaoSegundos: 354,
      anoLancamento: 1975,
      artistaPrincipalId: 1,
      artistasParticipantesIds: [],
      albumId: 10,
      generos: ['Rock']
    };

    service.cadastrarMusica(request).subscribe();

    const requisicao = httpMock.expectOne(apiAdminUrl);

    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual(request);

    requisicao.flush({
      id: 1,
      titulo: request.titulo,
      letra: null,
      duracaoSegundos: request.duracaoSegundos,
      anoLancamento: request.anoLancamento,
      artistaPrincipal: { id: 1, nome: 'Queen' },
      album: {
        id: 10,
        titulo: 'A Night at the Opera',
        anoLancamento: 1975,
        capaUrl: null
      },
      artistasParticipantes: [],
      generos: [{ id: 1, nome: 'Rock' }]
    });
  });

  it('deve buscar música por ID no endpoint público', () => {
    const respostaEsperada = musicaDetalhadaDeExemplo();

    service.buscarMusicaPorId(7).subscribe(resposta => {
      expect(resposta).toEqual(respostaEsperada);
    });

    const requisicao = httpMock.expectOne(`${apiUrl}/7`);

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush(respostaEsperada);
  });

  it('deve atualizar música por PUT no endpoint administrativo', () => {
    const request: MusicaRequest = {
      titulo: 'Música atualizada',
      letra: 'Texto atualizado de teste.',
      duracaoSegundos: 240,
      anoLancamento: 2026,
      artistaPrincipalId: 2,
      artistasParticipantesIds: [4],
      albumId: 3,
      generos: ['Gênero de teste']
    };
    const respostaEsperada: MusicaResponse = {
      ...musicaDetalhadaDeExemplo(),
      titulo: request.titulo,
      letra: request.letra ?? null,
      duracaoSegundos: request.duracaoSegundos
    };

    service.atualizarMusica(7, request).subscribe(resposta => {
      expect(resposta).toEqual(respostaEsperada);
    });

    const requisicao = httpMock.expectOne(`${apiAdminUrl}/7`);

    expect(requisicao.request.method).toBe('PUT');
    expect(requisicao.request.body).toEqual(request);

    requisicao.flush(respostaEsperada);
  });

  it('deve excluir música por DELETE no endpoint administrativo', () => {
    let requisicaoConcluida = false;

    service.excluirMusica(7).subscribe({
      complete: () => {
        requisicaoConcluida = true;
      }
    });

    const requisicao = httpMock.expectOne(`${apiAdminUrl}/7`);

    expect(requisicao.request.method).toBe('DELETE');

    requisicao.flush(null);

    expect(requisicaoConcluida).toBe(true);
  });
});
