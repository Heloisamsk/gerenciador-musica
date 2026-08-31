import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';

import { Cadastro } from './cadastro';

describe('Cadastro (integração com AuthService)', () => {
  let component: Cadastro;
  let fixture: ComponentFixture<Cadastro>;
  let httpMock: HttpTestingController;
  let router: Router;

  const apiBaseUrl = 'http://localhost:8080/api/auth';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cadastro],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Cadastro);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    fixture.detectChanges();

    /*
     * O template também renderiza o app-album-backdrop, que busca
     * as capas públicas no ngOnInit. Sem isso, httpMock.verify()
     * encontraria essa requisição em aberto em todo teste.
     */
    httpMock
      .expectOne('http://localhost:8080/api/public/albuns/capas')
      .flush([]);

    await fixture.whenStable();
  });

  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });

  function preencherFormularioValido(): void {
    component.cadastroForm.setValue({
      nome: 'Maria',
      email: 'maria@email.com',
      senha: 'senha123',
    });
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('não deve exibir opção para cadastrar administrador', () => {
    const elemento: HTMLElement = fixture.nativeElement;

    expect(
      elemento.querySelector('[formControlName="role"]')
    ).toBeNull();

    expect(elemento.textContent).not.toContain('Admin');
  });

  it('não deve enviar requisição quando o formulário é inválido', () => {
    component.cadastroForm.setValue({
      nome: '',
      email: '',
      senha: '',
    });

    component.cadastrar();

    httpMock.expectNone(`${apiBaseUrl}/register`);

    expect(component.nome.touched).toBe(true);
    expect(component.email.touched).toBe(true);
    expect(component.senha.touched).toBe(true);
  });

  it('deve enviar cadastro sem o campo role', () => {
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    vi.spyOn(window, 'alert').mockImplementation(() => {});

    preencherFormularioValido();

    component.cadastrar();

    const requisicao =
      httpMock.expectOne(`${apiBaseUrl}/register`);

    expect(requisicao.request.method).toBe('POST');

    expect(requisicao.request.body).toEqual({
      nome: 'Maria',
      email: 'maria@email.com',
      senha: 'senha123',
    });

    expect(requisicao.request.body)
      .not.toHaveProperty('role');

    requisicao.flush({
      id: 1,
      nome: 'Maria',
      email: 'maria@email.com',
      role: 'USER',
    });
  });

  it('deve navegar para /login quando o cadastro for concluído', () => {
    const navigateSpy =
      vi.spyOn(router, 'navigate').mockResolvedValue(true);

    const alertSpy =
      vi.spyOn(window, 'alert').mockImplementation(() => {});

    preencherFormularioValido();

    component.cadastrar();

    httpMock
      .expectOne(`${apiBaseUrl}/register`)
      .flush({
        id: 1,
        nome: 'Maria',
        email: 'maria@email.com',
        role: 'USER',
      });

    expect(alertSpy).toHaveBeenCalledWith(
      'Cadastro realizado com sucesso!'
    );

    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('deve mostrar a mensagem do backend quando o e-mail já existir', () => {
    const navigateSpy =
      vi.spyOn(router, 'navigate');

    const alertSpy =
      vi.spyOn(window, 'alert').mockImplementation(() => {});

    preencherFormularioValido();

    component.cadastrar();

    httpMock
      .expectOne(`${apiBaseUrl}/register`)
      .flush(
        {
          message:
            'Já existe um usuário cadastrado com este e-mail.',
        },
        {
          status: 409,
          statusText: 'Conflict',
        }
      );

    expect(alertSpy).toHaveBeenCalledWith(
      'Já existe um usuário cadastrado com este e-mail.'
    );

    expect(navigateSpy).not.toHaveBeenCalled();
  });

  it('deve mostrar a mensagem do backend para dados inválidos', () => {
    const alertSpy =
      vi.spyOn(window, 'alert').mockImplementation(() => {});
    vi.spyOn(console, 'error').mockImplementation(() => {});

    preencherFormularioValido();
    component.cadastrar();

    httpMock
      .expectOne(`${apiBaseUrl}/register`)
      .flush(
        {
          message: 'O e-mail informado é inválido.'
        },
        {
          status: 400,
          statusText: 'Bad Request'
        }
      );

    expect(alertSpy).toHaveBeenCalledWith(
      'Erro de validação: O e-mail informado é inválido.'
    );
  });

  it('deve usar mensagem padrão para erro 400 sem mensagem', () => {
    const alertSpy =
      vi.spyOn(window, 'alert').mockImplementation(() => {});
    vi.spyOn(console, 'error').mockImplementation(() => {});

    preencherFormularioValido();
    component.cadastrar();

    httpMock
      .expectOne(`${apiBaseUrl}/register`)
      .flush(
        {},
        {
          status: 400,
          statusText: 'Bad Request'
        }
      );

    expect(alertSpy).toHaveBeenCalledWith(
      'Erro de validação: Dados inválidos. Verifique as informações digitadas.'
    );
  });

  it('deve informar o status para erro inesperado', () => {
    const alertSpy =
      vi.spyOn(window, 'alert').mockImplementation(() => {});
    vi.spyOn(console, 'error').mockImplementation(() => {});

    preencherFormularioValido();
    component.cadastrar();

    httpMock
      .expectOne(`${apiBaseUrl}/register`)
      .flush(
        {},
        {
          status: 500,
          statusText: 'Internal Server Error'
        }
      );

    expect(alertSpy).toHaveBeenCalledWith(
      'Erro ao realizar o cadastro. Status: 500'
    );
  });

  it('deve rejeitar uma senha com menos de seis caracteres', () => {
    component.cadastroForm.setValue({
      nome: 'Maria',
      email: 'maria@email.com',
      senha: '12345',
    });

    expect(component.cadastroForm.invalid).toBe(true);
    expect(component.senha.hasError('minlength')).toBe(true);

    component.cadastrar();

    httpMock.expectNone(`${apiBaseUrl}/register`);
  });
});
