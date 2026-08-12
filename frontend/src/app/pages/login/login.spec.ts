import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';

import { Login } from './login';
import { LoginResponse } from '../../services/auth';

/*
 * Teste de INTEGRAÇÃO entre Component e Service: aqui o AuthService
 * NÃO é mockado, é o service de verdade. Só a camada HTTP é
 * substituída (provideHttpClientTesting), simulando um backend
 * falso — exatamente a ideia do json-server citada no enunciado,
 * só que resolvida com a ferramenta de teste do próprio Angular.
 *
 * Isso garante que Component e Service conversam corretamente:
 * o clique no botão realmente aciona o AuthService.login(), que
 * realmente monta a requisição, e a resposta realmente volta para
 * o Component tratar (navegação em caso de sucesso, alerta em caso
 * de erro).
 */
describe('Login (integração com AuthService)', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let httpMock: HttpTestingController;
  let router: Router;

  const apiBaseUrl = 'http://localhost:8080/api/auth';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('não deve chamar o AuthService quando o formulário é inválido', () => {
    component.loginForm.setValue({ email: '', senha: '' });

    component.entrar();

    httpMock.expectNone(`${apiBaseUrl}/login`);
    expect(component.email.touched).toBe(true);
    expect(component.senha.touched).toBe(true);
  });

  it('deve navegar para /home quando o login é bem-sucedido', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.loginForm.setValue({
      email: 'maria@email.com',
      senha: 'senha123',
    });

    component.entrar();

    httpMock.expectOne(`${apiBaseUrl}/login`).flush({
      token: 'token-fake',
      nome: 'Maria',
      email: 'maria@email.com',
      role: 'USER',
    } as LoginResponse);

    expect(navigateSpy).toHaveBeenCalledWith(['/home']);
  });

  it('deve mostrar um alerta e não navegar quando as credenciais são inválidas', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

    component.loginForm.setValue({
      email: 'maria@email.com',
      senha: 'senha-errada',
    });

    component.entrar();

    httpMock
      .expectOne(`${apiBaseUrl}/login`)
      .flush(
        { message: 'E-mail ou senha inválidos.' },
        { status: 401, statusText: 'Unauthorized' },
      );

    expect(alertSpy).toHaveBeenCalled();
    expect(navigateSpy).not.toHaveBeenCalled();
  });
});