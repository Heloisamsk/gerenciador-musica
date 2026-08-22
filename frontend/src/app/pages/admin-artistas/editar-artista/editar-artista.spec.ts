import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import {
  ActivatedRoute,
  ParamMap,
  Router,
  convertToParamMap,
  provideRouter
} from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { ArtistaRequest } from '../../../models/ArtistaRequest';
import { ArtistaResponse } from '../../../models/ArtistaResponse';
import { AdminArtistaService } from '../../../services/admin-artista';
import { FormularioArtista } from '../formulario-artista/formulario-artista';
import { EditarArtista } from './editar-artista';

describe('EditarArtista', () => {
  let component: EditarArtista;
  let fixture: ComponentFixture<EditarArtista>;
  let router: Router;

  const buscarPorId = vi.fn();
  const atualizar = vi.fn();
  const routeMock: { snapshot: { paramMap: ParamMap } } = {
    snapshot: {
      paramMap: convertToParamMap({ id: '7' })
    }
  };

  const artista: ArtistaResponse = {
    idArtista: 7,
    nome: 'Queen',
    nomeCompleto: 'Queen',
    descricao: 'Banda britânica de rock.',
    fotoPerfilUrl: 'https://exemplo.com/queen.jpg'
  };

  const requestAtualizado: ArtistaRequest = {
    nome: 'Queen + Adam Lambert',
    nomeCompleto: 'Queen e Adam Lambert',
    descricao: 'Projeto musical em atividade.',
    fotoPerfilUrl: null
  };

  beforeEach(async () => {
    buscarPorId.mockReset();
    atualizar.mockReset();
    buscarPorId.mockReturnValue(of(artista));
    routeMock.snapshot.paramMap = convertToParamMap({ id: '7' });

    await TestBed.configureTestingModule({
      imports: [EditarArtista],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: routeMock
        },
        {
          provide: AdminArtistaService,
          useValue: { buscarPorId, atualizar }
        }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  function criarComponente(id: string | null = '7'): void {
    routeMock.snapshot.paramMap = id === null
      ? convertToParamMap({})
      : convertToParamMap({ id });
    fixture = TestBed.createComponent(EditarArtista);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function obterFormulario(): FormularioArtista {
    return fixture.debugElement.query(
      By.directive(FormularioArtista)
    ).componentInstance as FormularioArtista;
  }

  it('deve carregar o artista da rota e preencher o formulário', () => {
    criarComponente();

    const formulario = obterFormulario();

    expect(buscarPorId).toHaveBeenCalledWith(7);
    expect(formulario.modo()).toBe('edicao');
    expect(formulario.exibirCancelar()).toBe(true);
    expect(formulario.formulario.getRawValue()).toEqual({
      nome: artista.nome,
      nomeCompleto: artista.nomeCompleto,
      descricao: artista.descricao,
      fotoPerfilUrl: artista.fotoPerfilUrl
    });
  });

  it.each([
    null,
    '0',
    '-1',
    '1.5',
    'abc',
    '9007199254740992'
  ])('deve rejeitar o identificador inválido %s', id => {
    criarComponente(id);

    const elemento = fixture.nativeElement as HTMLElement;

    expect(buscarPorId).not.toHaveBeenCalled();
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('O identificador do artista é inválido.');
    expect(fixture.debugElement.query(By.directive(FormularioArtista)))
      .toBeNull();
  });

  it('deve exibir o carregamento até receber os dados', () => {
    const resposta = new Subject<ArtistaResponse>();
    buscarPorId.mockReturnValue(resposta.asObservable());

    criarComponente();

    expect(component.carregandoDados()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Carregando dados do artista...');

    resposta.next(artista);
    resposta.complete();
    fixture.detectChanges();

    expect(component.carregandoDados()).toBe(false);
    expect(obterFormulario()).toBeTruthy();
  });

  it('deve informar quando o artista não for encontrado', () => {
    buscarPorId.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 404 })
    ));

    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('Artista não encontrado.');
    expect(fixture.debugElement.query(By.directive(FormularioArtista)))
      .toBeNull();
  });

  it('deve bloquear envios duplicados enquanto salva', () => {
    const resposta = new Subject<ArtistaResponse>();
    atualizar.mockReturnValue(resposta.asObservable());
    criarComponente();

    component.salvar(requestAtualizado);
    component.salvar(requestAtualizado);

    expect(atualizar).toHaveBeenCalledOnce();
    expect(atualizar).toHaveBeenCalledWith(7, requestAtualizado);
    expect(component.salvando()).toBe(true);

    resposta.complete();
    expect(component.salvando()).toBe(false);
  });

  it('deve atualizar e redirecionar com uma mensagem de sucesso', () => {
    const response: ArtistaResponse = {
      idArtista: 7,
      nome: requestAtualizado.nome,
      nomeCompleto: requestAtualizado.nomeCompleto,
      descricao: requestAtualizado.descricao,
      fotoPerfilUrl: requestAtualizado.fotoPerfilUrl ?? null
    };
    atualizar.mockReturnValue(of(response));
    criarComponente();

    component.salvar(requestAtualizado);

    expect(atualizar).toHaveBeenCalledWith(7, requestAtualizado);
    expect(component.mensagemSucesso())
      .toBe('Artista Queen + Adam Lambert atualizado com sucesso!');
    expect(router.navigate).toHaveBeenCalledWith(
      ['/admin/banco/artistas'],
      {
        state: {
          mensagemSucesso:
            'Artista Queen + Adam Lambert atualizado com sucesso!'
        }
      }
    );
  });

  it('deve cancelar e retornar à listagem', () => {
    criarComponente();

    obterFormulario().cancelar.emit();

    expect(router.navigate).toHaveBeenCalledWith([
      '/admin/banco/artistas'
    ]);
  });

  it.each([
    [400, 'Existem dados inválidos no formulário.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não possui permissão para editar artistas.'],
    [404, 'Artista não encontrado.'],
    [409, 'Já existe outro artista com esse nome.'],
    [500, 'Ocorreu um erro no servidor ao atualizar o artista.']
  ])('deve tratar o erro HTTP %i ao atualizar', (status, mensagem) => {
    atualizar.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status })
    ));
    criarComponente();

    component.salvar(requestAtualizado);

    expect(component.mensagemErro()).toBe(mensagem);
    expect(component.dadosIniciais()).not.toBeNull();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('não deve limpar o formulário quando a atualização falhar', () => {
    atualizar.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 409 })
    ));
    criarComponente();
    const formulario = obterFormulario();
    formulario.formulario.setValue({
      ...requestAtualizado,
      fotoPerfilUrl: ''
    });

    formulario.submeter();
    fixture.detectChanges();

    expect(formulario.formulario.getRawValue()).toEqual({
      ...requestAtualizado,
      fotoPerfilUrl: ''
    });
    expect((fixture.nativeElement as HTMLElement)
      .querySelector('[role="alert"]')?.textContent)
      .toContain('Já existe outro artista com esse nome.');
  });
});
