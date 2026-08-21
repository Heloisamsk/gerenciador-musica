import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { AdminAlbumService } from './admin-album.service';
import { AlbumRequest } from '../models/AlbumRequestModel';
import { AlbumResponse } from '../models/AlbumResponse';
import { environment } from '../../environments/environment';

describe('AdminAlbumService', () => {
  let service: AdminAlbumService;
  let httpMock: HttpTestingController;
  const apiCadastroUrl = `${environment.apiUrl}/api/admin/albuns`;
  const apiCatalogoUrl = `${environment.apiUrl}/api/albuns`;

  const requestValido: AlbumRequest = {
    titulo: 'A Night at the Opera',
    idArtista: 1,
    anoLancamento: 1975,
    capaUrl: null
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

  afterEach(() => httpMock.verify());

  it('deve cadastrar álbum com sucesso', () => {
    const respostaEsperada: AlbumResponse = {
      idAlbum: 1,
      titulo: 'A Night at the Opera',
      anoLancamento: 1975,
      capaUrl: null,
      artista: {
        idArtista: 1,
        nome: 'Queen',
        nomeCompleto: 'Queen',
        descricao: 'Banda britânica de rock.',
        fotoPerfilUrl: null
      }
    };

    service.cadastrarAlbum(requestValido).subscribe((resposta) => {
      expect(resposta).toEqual(respostaEsperada);
    });

    const req = httpMock.expectOne(apiCadastroUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(requestValido);
    req.flush(respostaEsperada);
  });

  it('deve propagar erro 400 (dados inválidos)', () => {
    service.cadastrarAlbum(requestValido).subscribe({
      next: () => { throw new Error('deveria ter lançado erro'); },
      error: (erro) => expect(erro.status).toBe(400)
    });

    const req = httpMock.expectOne(apiCadastroUrl);
    req.flush({ fieldErrors: { titulo: 'obrigatório' } }, { status: 400, statusText: 'Bad Request' });
  });

  it('deve propagar erro 401 (não autenticado)', () => {
    service.cadastrarAlbum(requestValido).subscribe({
      next: () => { throw new Error('deveria ter lançado erro'); },
      error: (erro) => expect(erro.status).toBe(401)
    });

    const req = httpMock.expectOne(apiCadastroUrl);
    req.flush({}, { status: 401, statusText: 'Unauthorized' });
  });

  it('deve propagar erro 403 (sem permissão)', () => {
    service.cadastrarAlbum(requestValido).subscribe({
      next: () => { throw new Error('deveria ter lançado erro'); },
      error: (erro) => expect(erro.status).toBe(403)
    });

    const req = httpMock.expectOne(apiCadastroUrl);
    req.flush({}, { status: 403, statusText: 'Forbidden' });
  });

  it('deve propagar erro 404 (artista não encontrado)', () => {
    service.cadastrarAlbum(requestValido).subscribe({
      next: () => { throw new Error('deveria ter lançado erro'); },
      error: (erro) => expect(erro.status).toBe(404)
    });

    const req = httpMock.expectOne(apiCadastroUrl);
    req.flush({}, { status: 404, statusText: 'Not Found' });
  });

  it('deve propagar erro 409 (álbum duplicado)', () => {
    service.cadastrarAlbum(requestValido).subscribe({
      next: () => { throw new Error('deveria ter lançado erro'); },
      error: (erro) => expect(erro.status).toBe(409)
    });

    const req = httpMock.expectOne(apiCadastroUrl);
    req.flush({ message: 'Esse álbum já está cadastrado.' }, { status: 409, statusText: 'Conflict' });
  });

  it('deve listar somente os álbuns do artista informado', () => {
    const respostaEsperada: AlbumResponse[] = [
      {
        idAlbum: 1,
        titulo: 'A Night at the Opera',
        anoLancamento: 1975,
        capaUrl: null,
        artista: {
          idArtista: 1,
          nome: 'Queen',
          nomeCompleto: 'Queen',
          descricao: 'Banda britânica de rock.',
          fotoPerfilUrl: null
        }
      }
    ];

    service.listarAlbunsPorArtista(1).subscribe(resposta => {
      expect(resposta).toEqual(respostaEsperada);
    });

    const req = httpMock.expectOne(
      `${apiCatalogoUrl}?artistaId=1`
    );

    expect(req.request.method).toBe('GET');
    req.flush(respostaEsperada);
  });
});
