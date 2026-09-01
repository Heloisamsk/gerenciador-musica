import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { AdminAlbumService } from './admin-album.service';
import { AlbumAtualizacaoRequest } from '../models/AlbumAtualizacaoRequest';
import { AlbumRequest } from '../models/AlbumRequestModel';
import { AlbumResponse } from '../models/AlbumResponse';
import { environment } from '../../environments/environment';

describe('AdminAlbumService', () => {
  let service: AdminAlbumService;
  let httpMock: HttpTestingController;

  const apiAdminUrl =
    `${environment.apiUrl}/api/admin/albuns`;

  const apiPublicaUrl =
    `${environment.apiUrl}/api/albuns`;

  const requestValido: AlbumRequest = {
    titulo: 'A Night at the Opera',
    idArtista: 1,
    anoLancamento: 1975,
    capaUrl: null
  };

  const atualizacaoValida: AlbumAtualizacaoRequest = {
    titulo: 'A Night at the Opera - Remastered',
    anoLancamento: 2011,
    capaUrl: 'https://example.com/capa-remastered.jpg'
  };

  const respostaEsperada: AlbumResponse = {
    idAlbum: 1,
    titulo: 'A Night at the Opera',
    anoLancamento: 1975,
    capaUrl: null,
    artista: {
      id: 1,
      nome: 'Queen',
      nomeCompleto: 'Queen',
      descricao: 'Banda britânica de rock.',
      fotoPerfilUrl: null
    },
    curtida: false
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AdminAlbumService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AdminAlbumService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve cadastrar álbum com sucesso', () => {
    service
      .cadastrarAlbum(requestValido)
      .subscribe(resposta => {
        expect(resposta).toEqual(respostaEsperada);
      });

    const requisicao = httpMock.expectOne(apiAdminUrl);

    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual(requestValido);

    requisicao.flush(respostaEsperada);
  });

  it('deve propagar erro 400 (dados inválidos)', () => {
    service
      .cadastrarAlbum(requestValido)
      .subscribe({
        next: () => {
          throw new Error('deveria ter lançado erro');
        },
        error: erro => {
          expect(erro.status).toBe(400);
        }
      });

    const requisicao = httpMock.expectOne(apiAdminUrl);

    requisicao.flush(
      {
        fieldErrors: {
          titulo: 'obrigatório'
        }
      },
      {
        status: 400,
        statusText: 'Bad Request'
      }
    );
  });

  it('deve propagar erro 401 (não autenticado)', () => {
    service
      .cadastrarAlbum(requestValido)
      .subscribe({
        next: () => {
          throw new Error('deveria ter lançado erro');
        },
        error: erro => {
          expect(erro.status).toBe(401);
        }
      });

    const requisicao = httpMock.expectOne(apiAdminUrl);

    requisicao.flush(
      {},
      {
        status: 401,
        statusText: 'Unauthorized'
      }
    );
  });

  it('deve propagar erro 403 (sem permissão)', () => {
    service
      .cadastrarAlbum(requestValido)
      .subscribe({
        next: () => {
          throw new Error('deveria ter lançado erro');
        },
        error: erro => {
          expect(erro.status).toBe(403);
        }
      });

    const requisicao = httpMock.expectOne(apiAdminUrl);

    requisicao.flush(
      {},
      {
        status: 403,
        statusText: 'Forbidden'
      }
    );
  });

  it('deve propagar erro 404 (artista não encontrado)', () => {
    service
      .cadastrarAlbum(requestValido)
      .subscribe({
        next: () => {
          throw new Error('deveria ter lançado erro');
        },
        error: erro => {
          expect(erro.status).toBe(404);
        }
      });

    const requisicao = httpMock.expectOne(apiAdminUrl);

    requisicao.flush(
      {},
      {
        status: 404,
        statusText: 'Not Found'
      }
    );
  });

  it('deve propagar erro 409 (álbum duplicado)', () => {
    service
      .cadastrarAlbum(requestValido)
      .subscribe({
        next: () => {
          throw new Error('deveria ter lançado erro');
        },
        error: erro => {
          expect(erro.status).toBe(409);
        }
      });

    const requisicao = httpMock.expectOne(apiAdminUrl);

    requisicao.flush(
      {
        message: 'Esse álbum já está cadastrado.'
      },
      {
        status: 409,
        statusText: 'Conflict'
      }
    );
  });

  it('deve listar somente os álbuns do artista informado', () => {
    service
      .listarAlbunsPorArtista(1)
      .subscribe(resposta => {
        expect(resposta).toEqual([respostaEsperada]);
      });

    const requisicao = httpMock.expectOne(
      `${apiPublicaUrl}?artistaId=1`
    );

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush([respostaEsperada]);
  });

  it('deve listar todos os álbuns pelo endpoint público', () => {
    service.listarAlbuns().subscribe(resposta => {
      expect(resposta).toEqual([respostaEsperada]);
    });

    const requisicao = httpMock.expectOne(apiPublicaUrl);

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush([respostaEsperada]);
  });

  it('deve buscar um álbum por ID no endpoint público', () => {
    service.buscarPorId(1).subscribe(resposta => {
      expect(resposta).toEqual(respostaEsperada);
    });

    const requisicao = httpMock.expectOne(
      `${apiPublicaUrl}/1`
    );

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush(respostaEsperada);
  });

  it('deve atualizar um álbum por PUT sem enviar o artista', () => {
    const respostaAtualizada: AlbumResponse = {
      ...respostaEsperada,
      titulo: atualizacaoValida.titulo,
      anoLancamento: atualizacaoValida.anoLancamento,
      capaUrl: atualizacaoValida.capaUrl ?? null
    };

    service
      .atualizarAlbum(1, atualizacaoValida)
      .subscribe(resposta => {
        expect(resposta).toEqual(respostaAtualizada);
      });

    const requisicao = httpMock.expectOne(
      `${apiAdminUrl}/1`
    );

    expect(requisicao.request.method).toBe('PUT');
    expect(requisicao.request.body).toEqual(atualizacaoValida);
    expect(requisicao.request.body.idArtista).toBeUndefined();

    requisicao.flush(respostaAtualizada);
  });

  it('deve excluir um álbum por DELETE', () => {
    let requisicaoConcluida = false;

    service.excluirAlbum(1).subscribe({
      complete: () => {
        requisicaoConcluida = true;
      }
    });

    const requisicao = httpMock.expectOne(
      `${apiAdminUrl}/1`
    );

    expect(requisicao.request.method).toBe('DELETE');

    requisicao.flush(null);

    expect(requisicaoConcluida).toBe(true);
  });
});
