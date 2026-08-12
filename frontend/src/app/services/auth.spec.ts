import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AuthService, LoginResponse } from './auth';

/*
 * Teste de UNIDADE do AuthService: testamos o service isolado do
 * resto da aplicação (nenhum Component é criado aqui).
 *
 * O HttpClient real faria uma chamada de rede de verdade, então
 * usamos provideHttpClientTesting(), que substitui o HttpClient por
 * uma versão "de mentira": as chamadas ficam em espera e nós mesmos
 * decidimos (com httpMock.expectOne(...).flush(...)) qual resposta o
 * "servidor falso" devolve.
 */
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const apiBaseUrl = 'http://localhost:8080/api/auth';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    localStorage.clear();
  });

  afterEach(() => {
    // Garante que nenhuma requisição ficou pendura sem resposta.
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('deve enviar POST para /login com as credenciais informadas', () => {
    const credenciais = { email: 'maria@email.com', senha: 'senha123' };

    service.login(credenciais).subscribe();

    const requisicao = httpMock.expectOne(`${apiBaseUrl}/login`);

    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual(credenciais);

    requisicao.flush({
      token: 'token-fake',
      nome: 'Maria',
      email: 'maria@email.com',
      role: 'USER',
    } as LoginResponse);
  });

  it('deve guardar token e role no localStorage após login com sucesso', () => {
    service.login({ email: 'maria@email.com', senha: 'senha123' }).subscribe();

    httpMock.expectOne(`${apiBaseUrl}/login`).flush({
      token: 'token-fake',
      nome: 'Maria',
      email: 'maria@email.com',
      role: 'ADMIN',
    } as LoginResponse);

    expect(localStorage.getItem('token')).toBe('token-fake');
    expect(localStorage.getItem('role')).toBe('ADMIN');
    expect(service.isAutenticado()).toBe(true);
    expect(service.getRole()).toBe('ADMIN');
  });

  it('deve limpar token e role do localStorage após logout', () => {
    localStorage.setItem('token', 'token-fake');
    localStorage.setItem('role', 'USER');

    service.logout().subscribe();

    httpMock
      .expectOne(`${apiBaseUrl}/logout`)
      .flush({ mensagem: 'Logout realizado com sucesso.' });

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('role')).toBeNull();
    expect(service.isAutenticado()).toBe(false);
  });

  it('isAutenticado deve retornar false quando não há token salvo', () => {
    expect(service.isAutenticado()).toBe(false);
  });
});