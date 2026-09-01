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

import { AlbumAtualizacaoRequest } from '../../../models/AlbumAtualizacaoRequest';
import { AlbumResponse } from '../../../models/AlbumResponse';
import { AdminAlbumService } from '../../../services/admin-album.service';
import { FormularioAlbum } from '../formulario-album/formulario-album';
import { EditarAlbum } from './editar-album';

describe('EditarAlbum', () => {
  let component: EditarAlbum;
  let fixture: ComponentFixture<EditarAlbum>;
  let router: Router;

  const buscarPorId = vi.fn();
  const atualizarAlbum = vi.fn();
  const routeMock: { snapshot: { paramMap: ParamMap } } = {
    snapshot: {
      paramMap: convertToParamMap({ id: '10' })
    }
  };

  const album: AlbumResponse = {
    idAlbum: 10,
    titulo: 'A Night at the Opera',
    anoLancamento: 1975,
    capaUrl: 'https://example.com/capa.jpg',
    artista: {
      id: 7,
      nome: 'Queen',
      nomeCompleto: 'Queen',
      descricao: 'Banda britânica de rock.',
      fotoPerfilUrl: null
    },
    curtida: false
  };

  const requestAtualizado: AlbumAtualizacaoRequest = {
    titulo: 'A Night at the Opera - Remastered',
    anoLancamento: 2011,
    capaUrl: null
  };

  beforeEach(async () => {
    buscarPorId.mockReset();
    atualizarAlbum.mockReset();
    buscarPorId.mockReturnValue(of(album));
    routeMock.snapshot.paramMap = convertToParamMap({ id: '10' });

    await TestBed.configureTestingModule({
      imports: [EditarAlbum],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: routeMock
        },
        {
          provide: AdminAlbumService,
          useValue: { buscarPorId, atualizarAlbum }
        }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  function criarComponente(id: string | null = '10'): void {
    routeMock.snapshot.paramMap = id === null
      ? convertToParamMap({})
      : convertToParamMap({ id });
    fixture = TestBed.createComponent(EditarAlbum);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function obterFormulario(): FormularioAlbum {
    return fixture.debugElement.query(
      By.directive(FormularioAlbum)
    ).componentInstance as FormularioAlbum;
  }

  it('deve carregar o álbum e preencher o formulário de edição', () => {
    criarComponente();

    const formulario = obterFormulario();
    const elemento = fixture.nativeElement as HTMLElement;

    expect(buscarPorId).toHaveBeenCalledWith(10);
    expect(formulario.modo()).toBe('edicao');
    expect(formulario.formulario.getRawValue()).toEqual({
      titulo: album.titulo,
      idArtista: album.artista.id,
      anoLancamento: album.anoLancamento,
      capaUrl: album.capaUrl
    });
    expect(elemento.querySelector('select')).toBeNull();
    expect(elemento.querySelector<HTMLInputElement>(
      '#artistaResponsavel'
    )?.value).toBe('Queen');
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
      .toContain('O identificador do álbum é inválido.');
    expect(fixture.debugElement.query(By.directive(FormularioAlbum)))
      .toBeNull();
  });

  it('deve exibir carregamento enquanto busca o álbum', () => {
    const resposta = new Subject<AlbumResponse>();
    buscarPorId.mockReturnValue(resposta.asObservable());

    criarComponente();

    expect(component.carregandoDados()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Carregando dados do álbum...');

    resposta.next(album);
    resposta.complete();
    fixture.detectChanges();

    expect(component.carregandoDados()).toBe(false);
    expect(obterFormulario()).toBeTruthy();
  });

  it('deve informar quando o álbum não for encontrado', () => {
    buscarPorId.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 404 })
    ));

    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('Álbum não encontrado.');
    expect(fixture.debugElement.query(By.directive(FormularioAlbum)))
      .toBeNull();
  });

  it('deve bloquear atualizações duplicadas enquanto salva', () => {
    const resposta = new Subject<AlbumResponse>();
    atualizarAlbum.mockReturnValue(resposta.asObservable());
    criarComponente();

    component.salvar(requestAtualizado);
    component.salvar(requestAtualizado);

    expect(atualizarAlbum).toHaveBeenCalledOnce();
    expect(atualizarAlbum).toHaveBeenCalledWith(
      10,
      requestAtualizado
    );
    expect(component.salvando()).toBe(true);

    resposta.complete();
    expect(component.salvando()).toBe(false);
  });

  it('deve atualizar sem artista e retornar à listagem', () => {
    const response: AlbumResponse = {
      ...album,
      ...requestAtualizado
    };
    atualizarAlbum.mockReturnValue(of(response));
    criarComponente();

    obterFormulario().formulario.patchValue({
      titulo: requestAtualizado.titulo,
      anoLancamento: requestAtualizado.anoLancamento,
      capaUrl: ''
    });
    obterFormulario().submeter();

    expect(atualizarAlbum).toHaveBeenCalledWith(
      10,
      requestAtualizado
    );
    expect(atualizarAlbum.mock.calls[0][1].idArtista)
      .toBeUndefined();
    expect(component.mensagemSucesso()).toBe(
      'Álbum A Night at the Opera - Remastered atualizado com sucesso!'
    );
    expect(router.navigate).toHaveBeenCalledWith(
      ['/admin/banco/albuns'],
      {
        state: {
          mensagemSucesso:
            'Álbum A Night at the Opera - Remastered atualizado com sucesso!'
        }
      }
    );
  });

  it('deve cancelar e retornar à listagem', () => {
    criarComponente();

    obterFormulario().cancelar.emit();

    expect(router.navigate).toHaveBeenCalledWith([
      '/admin/banco/albuns'
    ]);
  });

  it.each([
    [400, 'Existem dados inválidos no formulário.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não possui permissão para editar álbuns.'],
    [404, 'Álbum não encontrado.'],
    [409, 'Já existe outro álbum com esse título, artista e ano.'],
    [500, 'Ocorreu um erro no servidor ao atualizar o álbum.']
  ])('deve tratar o erro HTTP %i ao atualizar', (status, mensagem) => {
    atualizarAlbum.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status })
    ));
    criarComponente();

    component.salvar(requestAtualizado);

    expect(component.mensagemErro()).toBe(mensagem);
    expect(component.album()).toEqual(album);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('não deve limpar os campos quando a atualização falhar', () => {
    atualizarAlbum.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 409 })
    ));
    criarComponente();
    const formulario = obterFormulario();
    formulario.formulario.patchValue({
      titulo: requestAtualizado.titulo,
      anoLancamento: requestAtualizado.anoLancamento,
      capaUrl: ''
    });

    formulario.submeter();
    fixture.detectChanges();

    expect(formulario.formulario.getRawValue()).toEqual({
      titulo: requestAtualizado.titulo,
      idArtista: album.artista.id,
      anoLancamento: requestAtualizado.anoLancamento,
      capaUrl: ''
    });
    expect((fixture.nativeElement as HTMLElement)
      .querySelector('[role="alert"]')?.textContent)
      .toContain('Já existe outro álbum');
  });
});
