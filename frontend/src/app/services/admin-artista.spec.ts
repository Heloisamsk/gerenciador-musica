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

  const apiAdminUrl =
    'http://localhost:8080/api/admin/artistas';

  const apiPublicaUrl =
    'http://localhost:8080/api/artistas';

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

    const request = httpTesting.expectOne(apiAdminUrl);

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(artistaRequest);

    request.flush(artistaResponse);
  });

  it('deve listar os artistas pelo endpoint de catálogo', () => {
    service.listarArtistas().subscribe(response => {
      expect(response).toEqual([artistaResponse]);
    });

    const request = httpTesting.expectOne(apiPublicaUrl);

    expect(request.request.method).toBe('GET');

    request.flush([artistaResponse]);
  });

  it('deve buscar um artista por ID no endpoint público', () => {
    service.buscarPorId(52).subscribe(response => {
      expect(response).toEqual(artistaResponse);
    });

    const request = httpTesting.expectOne(
      `${apiPublicaUrl}/52`
    );

    expect(request.request.method).toBe('GET');

    request.flush(artistaResponse);
  });

  it('deve atualizar um artista por PUT no endpoint administrativo', () => {
    service.atualizar(52, artistaRequest).subscribe(response => {
      expect(response).toEqual(artistaResponse);
    });

    const request = httpTesting.expectOne(
      `${apiAdminUrl}/52`
    );

    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(artistaRequest);

    request.flush(artistaResponse);
  });

  it('deve excluir um artista por DELETE no endpoint administrativo', () => {
    let requisicaoConcluida = false;

    service.excluir(52).subscribe({
      complete: () => {
        requisicaoConcluida = true;
      }
    });

    const request = httpTesting.expectOne(
      `${apiAdminUrl}/52`
    );

    expect(request.request.method).toBe('DELETE');

    request.flush(null);

    expect(requisicaoConcluida).toBe(true);
  });

  for (const status of [400, 401, 403, 409]) {
    it(`deve propagar o erro ${status}`, () => {
      let statusRecebido: number | undefined;

      service.cadastrar(artistaRequest).subscribe({
        error: (erro: HttpErrorResponse) => {
          statusRecebido = erro.status;
        }
      });

      const request = httpTesting.expectOne(apiAdminUrl);

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
