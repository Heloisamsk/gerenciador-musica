import {
  HttpClient,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';

import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(
          withInterceptors([authInterceptor])
        ),
        provideHttpClientTesting()
      ]
    });

    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('deve adicionar o JWT em uma requisição protegida', () => {
    localStorage.setItem('token', 'token-de-teste');

    http.get('http://localhost:8080/api/perfil').subscribe();

    const requisicao = httpTesting.expectOne(
      'http://localhost:8080/api/perfil'
    );

    expect(
      requisicao.request.headers.get('Authorization')
    ).toBe('Bearer token-de-teste');

    requisicao.flush({});
  });

  it('não deve adicionar Authorization quando não existir token', () => {
    http.get('http://localhost:8080/api/perfil').subscribe();

    const requisicao = httpTesting.expectOne(
      'http://localhost:8080/api/perfil'
    );

    expect(
      requisicao.request.headers.has('Authorization')
    ).toBe(false);

    requisicao.flush({});
  });

  it('não deve adicionar JWT ao endpoint de login', () => {
    localStorage.setItem('token', 'token-antigo');

    http.post(
      'http://localhost:8080/api/auth/login',
      {}
    ).subscribe();

    const requisicao = httpTesting.expectOne(
      'http://localhost:8080/api/auth/login'
    );

    expect(
      requisicao.request.headers.has('Authorization')
    ).toBe(false);

    requisicao.flush({});
  });

  it('deve limpar a sessão e redirecionar no erro 401', () => {
    localStorage.setItem('token', 'token-invalido');
    localStorage.setItem('role', 'USER');

    const navegacao = vi
      .spyOn(router, 'navigate')
      .mockResolvedValue(true);

    http.get('http://localhost:8080/api/perfil').subscribe({
      error: () => {}
    });

    const requisicao = httpTesting.expectOne(
      'http://localhost:8080/api/perfil'
    );

    requisicao.flush(
      {},
      {
        status: 401,
        statusText: 'Unauthorized'
      }
    );

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('role')).toBeNull();
    expect(navegacao).toHaveBeenCalledWith(['/login']);
  });

  it('deve manter a sessão no erro 403', () => {
    localStorage.setItem('token', 'token-valido');

    const aviso = vi
      .spyOn(console, 'warn')
      .mockImplementation(() => {});

    http.get('http://localhost:8080/api/admin').subscribe({
      error: () => {}
    });

    const requisicao = httpTesting.expectOne(
      'http://localhost:8080/api/admin'
    );

    requisicao.flush(
      {},
      {
        status: 403,
        statusText: 'Forbidden'
      }
    );

    expect(localStorage.getItem('token')).toBe('token-valido');
    expect(aviso).toHaveBeenCalled();
  });
});
