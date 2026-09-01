import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { CurtidaService } from './curtida';

describe('CurtidaService', () => {
  let service: CurtidaService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/api';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CurtidaService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(CurtidaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve curtir uma música', () => {
    service.curtirMusica(10).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/musicas/10/curtida`);
    expect(requisicao.request.method).toBe('POST');
    requisicao.flush(null);
  });

  it('deve descurtir uma música', () => {
    service.descurtirMusica(10).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/musicas/10/curtida`);
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null);
  });

  it('deve curtir um álbum', () => {
    service.curtirAlbum(20).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/albuns/20/curtida`);
    expect(requisicao.request.method).toBe('POST');
    requisicao.flush(null);
  });

  it('deve descurtir um álbum', () => {
    service.descurtirAlbum(20).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/albuns/20/curtida`);
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null);
  });

  it('deve repassar uma mensagem de erro quando o servidor falhar', () => {
    let erro: Error | undefined;
    service.curtirMusica(10).subscribe({
      error: e => erro = e
    });

    httpMock.expectOne(`${apiUrl}/musicas/10/curtida`)
      .flush('Falha interna', { status: 500, statusText: 'Server Error' });

    expect(erro?.message).toContain('500');
  });

  it('deve repassar uma mensagem de erro quando a rede falhar', () => {
    let erro: Error | undefined;
    service.curtirAlbum(20).subscribe({
      error: e => erro = e
    });

    httpMock.expectOne(`${apiUrl}/albuns/20/curtida`)
      .error(new ErrorEvent('network', { message: 'Sem conexão' }));

    expect(erro?.message).toContain('Sem conexão');
  });
});
