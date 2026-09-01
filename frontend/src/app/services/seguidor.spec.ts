import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SeguidorService } from './seguidor';

describe('SeguidorService', () => {
  let service: SeguidorService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/api';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SeguidorService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(SeguidorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve seguir um artista', () => {
    service.seguirArtista(5).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/artistas/5/seguidor`);
    expect(requisicao.request.method).toBe('POST');
    requisicao.flush(null);
  });

  it('deve deixar de seguir um artista', () => {
    service.deixarDeSeguirArtista(5).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/artistas/5/seguidor`);
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null);
  });

  it('deve seguir um usuário', () => {
    service.seguirUsuario(2).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/usuarios/2/seguidor`);
    expect(requisicao.request.method).toBe('POST');
    requisicao.flush(null);
  });

  it('deve deixar de seguir um usuário', () => {
    service.deixarDeSeguirUsuario(2).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/usuarios/2/seguidor`);
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null);
  });

  it('deve repassar uma mensagem de erro quando o servidor falhar', () => {
    let erro: Error | undefined;
    service.seguirUsuario(2).subscribe({
      error: e => erro = e
    });

    httpMock.expectOne(`${apiUrl}/usuarios/2/seguidor`)
      .flush('Não é possível seguir a si mesmo.', {
        status: 400,
        statusText: 'Bad Request'
      });

    expect(erro?.message).toContain('400');
  });

  it('deve repassar uma mensagem de erro quando a rede falhar', () => {
    let erro: Error | undefined;
    service.seguirArtista(5).subscribe({
      error: e => erro = e
    });

    httpMock.expectOne(`${apiUrl}/artistas/5/seguidor`)
      .error(new ErrorEvent('network', { message: 'Sem conexão' }));

    expect(erro?.message).toContain('Sem conexão');
  });
});
