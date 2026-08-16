import {
  HttpErrorResponse,
  provideHttpClient
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ArtistaRequest } from '../models/ArtistaRequest';
import { ArtistaResponse } from '../models/ArtistaResponse';

import { AdminArtistaService } from './admin-artista';

describe('AdminArtistaService', () => {
  let service: AdminArtistaService;
  let httpTesting: HttpTestingController;

  const apiUrl =
    'http://localhost:8080/api/admin/artistas';

  const artistaRequest: ArtistaRequest = {
    nome: 'Queen',
    nomeCompleto: 'Queen',
    descricao: 'Banda britânica de rock.',
    fotoPerfilUrl: null
  };

  const artistaResponse: ArtistaResponse = {
    idArtista: 52,
    nome: 'Queen',
    nomeCompleto: 'Queen',
    descricao: 'Banda britânica de rock.',
    fotoPerfilUrl: null
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AdminArtistaService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AdminArtistaService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve cadastrar um artista por POST', () => {
    service
      .cadastrar(artistaRequest)
      .subscribe(response => {
        expect(response).toEqual(artistaResponse);
      });

    const request = httpTesting.expectOne(apiUrl);

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(artistaRequest);

    request.flush(artistaResponse);
  });

  for (const status of [400, 401, 403, 409]) {
    it(`deve propagar o erro ${status}`, () => {
      let statusRecebido: number | undefined;

      service.cadastrar(artistaRequest).subscribe({
        error: (erro: HttpErrorResponse) => {
          statusRecebido = erro.status;
        }
      });

      const request = httpTesting.expectOne(apiUrl);

      request.flush(
        {
          status,
          message: 'Erro ao cadastrar artista.'
        },
        {
          status,
          statusText: 'Erro'
        }
      );

      expect(statusRecebido).toBe(status);
    });
  }
});
