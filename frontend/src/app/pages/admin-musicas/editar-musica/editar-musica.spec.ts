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

import type { MusicaRequest } from '../../../models/MusicaRequest';
import type { MusicaResponse } from '../../../models/MusicaResponse';
import { AdminAlbumService } from '../../../services/admin-album.service';
import { AdminArtistaService } from '../../../services/admin-artista';
import { AdminMusicaService } from '../../../services/admin-musica';
import { FormularioMusica } from '../formulario-musica/formulario-musica';
import { EditarMusica } from './editar-musica';

describe('EditarMusica', () => {
  let component: EditarMusica;
  let fixture: ComponentFixture<EditarMusica>;
  let router: Router;

  const buscarMusicaPorId = vi.fn();
  const atualizarMusica = vi.fn();
  const listarArtistas = vi.fn();
  const listarAlbunsPorArtista = vi.fn();

  const routeMock: { snapshot: { paramMap: ParamMap } } = {
    snapshot: {
      paramMap: convertToParamMap({ id: '31' })
    }
  };

  const musica: MusicaResponse = {
    id: 31,
    titulo: 'Música de Teste',
    letra: 'Texto original criado para o teste.',
    duracaoSegundos: 205,
    anoLancamento: 2024,
    artistaPrincipal: { id: 7, nome: 'Artista Principal' },
    album: {
      id: 11,
      titulo: 'Álbum de Teste',
      anoLancamento: 2024,
      capaUrl: null
    },
    artistasParticipantes: [
      { id: 8, nome: 'Artista Participante' }
    ],
    generos: [
      { id: 1, nome: 'Gênero Um' },
      { id: 2, nome: 'Gênero Dois' }
    ]
  };

  const requestAtualizado: MusicaRequest = {
    titulo: 'Música Atualizada',
    letra: null,
    duracaoSegundos: 230,
    anoLancamento: 2025,
    artistaPrincipalId: 7,
    artistasParticipantesIds: [8],
    albumId: 11,
    generos: ['Gênero Um', 'Gênero Dois']
  };

  const responseAtualizada: MusicaResponse = {
    ...musica,
    titulo: requestAtualizado.titulo,
    letra: requestAtualizado.letra ?? null,
    duracaoSegundos: requestAtualizado.duracaoSegundos,
    anoLancamento: requestAtualizado.anoLancamento
  };

  beforeEach(async () => {
    buscarMusicaPorId.mockReset();
    atualizarMusica.mockReset();
    listarArtistas.mockReset();
    listarAlbunsPorArtista.mockReset();
    buscarMusicaPorId.mockReturnValue(of(musica));
    atualizarMusica.mockReturnValue(of(responseAtualizada));
    listarArtistas.mockReturnValue(of([
      {
        idArtista: 7,
        nome: 'Artista Principal',
        nomeCompleto: 'Nome do Artista Principal',
        descricao: 'Descrição de teste.',
        fotoPerfilUrl: null
      },
      {
        idArtista: 8,
        nome: 'Artista Participante',
        nomeCompleto: 'Nome do Artista Participante',
        descricao: 'Descrição de teste.',
        fotoPerfilUrl: null
      }
    ]));
    listarAlbunsPorArtista.mockReturnValue(of([
      {
        idAlbum: 11,
        titulo: 'Álbum de Teste',
        anoLancamento: 2024,
        capaUrl: null,
        artista: {
          id: 7,
          nome: 'Artista Principal',
          nomeCompleto: 'Nome do Artista Principal',
          descricao: 'Descrição de teste.',
          fotoPerfilUrl: null
        }
      }
    ]));
    routeMock.snapshot.paramMap = convertToParamMap({ id: '31' });

    await TestBed.configureTestingModule({
      imports: [EditarMusica],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: routeMock
        },
        {
          provide: AdminMusicaService,
          useValue: {
            buscarMusicaPorId,
            atualizarMusica
          }
        },
        {
          provide: AdminArtistaService,
          useValue: { listarArtistas }
        },
        {
          provide: AdminAlbumService,
          useValue: { listarAlbunsPorArtista }
        }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  function criarComponente(id: string | null = '31'): void {
    routeMock.snapshot.paramMap = id === null
      ? convertToParamMap({})
      : convertToParamMap({ id });
    fixture = TestBed.createComponent(EditarMusica);
    component = fixture.componentInstance;
    fixture.detectChanges();
    fixture.detectChanges();
  }

  function obterFormulario(): FormularioMusica {
    return fixture.debugElement.query(
      By.directive(FormularioMusica)
    ).componentInstance as FormularioMusica;
  }

  it('deve carregar a música e preencher dependências e associações', () => {
    criarComponente();
    const formulario = obterFormulario();

    expect(buscarMusicaPorId).toHaveBeenCalledWith(31);
    expect(listarArtistas).toHaveBeenCalledOnce();
    expect(listarAlbunsPorArtista).toHaveBeenCalledWith(7);
    expect(formulario.modo()).toBe('edicao');
    expect(formulario.exibirCancelar()).toBe(true);
    expect(formulario.formulario.getRawValue()).toEqual({
      titulo: musica.titulo,
      letra: musica.letra,
      duracaoSegundos: musica.duracaoSegundos,
      anoLancamento: musica.anoLancamento,
      generosTexto: 'Gênero Um, Gênero Dois',
      artistaPrincipalId: 7,
      artistasParticipantesIds: [8],
      albumId: 11
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

    expect(buscarMusicaPorId).not.toHaveBeenCalled();
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('O identificador da música é inválido.');
    expect(fixture.debugElement.query(By.directive(FormularioMusica)))
      .toBeNull();
  });

  it('deve exibir carregamento até receber os dados', () => {
    const resposta = new Subject<MusicaResponse>();
    buscarMusicaPorId.mockReturnValue(resposta.asObservable());
    criarComponente();

    expect(component.carregandoDados()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Carregando dados da música...');

    resposta.next(musica);
    resposta.complete();
    fixture.detectChanges();
    fixture.detectChanges();

    expect(component.carregandoDados()).toBe(false);
    expect(obterFormulario()).toBeTruthy();
  });

  it('deve informar quando a música não for encontrada', () => {
    buscarMusicaPorId.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 404 })
    ));
    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('Música não encontrada.');
    expect(fixture.debugElement.query(By.directive(FormularioMusica)))
      .toBeNull();
  });

  it('deve bloquear envios duplicados durante a atualização', () => {
    const resposta = new Subject<MusicaResponse>();
    atualizarMusica.mockReturnValue(resposta.asObservable());
    criarComponente();

    component.salvar(requestAtualizado);
    component.salvar(requestAtualizado);

    expect(atualizarMusica).toHaveBeenCalledOnce();
    expect(atualizarMusica)
      .toHaveBeenCalledWith(31, requestAtualizado);
    expect(component.salvando()).toBe(true);

    resposta.complete();
    expect(component.salvando()).toBe(false);
  });

  it('deve atualizar e redirecionar com mensagem de sucesso', () => {
    criarComponente();

    component.salvar(requestAtualizado);

    expect(atualizarMusica)
      .toHaveBeenCalledWith(31, requestAtualizado);
    expect(component.musica()).toEqual(responseAtualizada);
    expect(component.mensagemSucesso())
      .toBe('Música Música Atualizada atualizada com sucesso!');
    expect(router.navigate).toHaveBeenCalledWith(
      ['/admin/banco/musicas'],
      {
        state: {
          mensagemSucesso:
            'Música Música Atualizada atualizada com sucesso!'
        }
      }
    );
  });

  it('deve cancelar e retornar à listagem', () => {
    criarComponente();

    obterFormulario().cancelar.emit();

    expect(router.navigate).toHaveBeenCalledWith([
      '/admin/banco/musicas'
    ]);
  });

  it.each([
    [0, 'Não foi possível conectar ao servidor.'],
    [400, 'Existem dados inválidos no formulário.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não possui permissão para editar músicas.'],
    [404, 'Música não encontrada.'],
    [409, 'Já existe outra música com esses dados.'],
    [500, 'Ocorreu um erro no servidor ao atualizar a música.'],
    [418, 'Não foi possível atualizar a música.']
  ])('deve tratar o erro HTTP %i ao atualizar', (status, mensagem) => {
    atualizarMusica.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status })
    ));
    criarComponente();

    component.salvar(requestAtualizado);

    expect(component.mensagemErro()).toBe(mensagem);
    expect(component.musica()).toEqual(musica);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('deve preservar os valores do formulário quando a atualização falhar', () => {
    atualizarMusica.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 409 })
    ));
    criarComponente();
    const formulario = obterFormulario();
    formulario.formulario.patchValue({
      titulo: requestAtualizado.titulo,
      letra: '',
      duracaoSegundos: requestAtualizado.duracaoSegundos,
      anoLancamento: requestAtualizado.anoLancamento
    });

    formulario.submeter();
    fixture.detectChanges();

    expect(formulario.formulario.controls.titulo.value)
      .toBe(requestAtualizado.titulo);
    expect(formulario.formulario.controls.letra.value).toBe('');
    expect((fixture.nativeElement as HTMLElement)
      .querySelector('[role="alert"]')?.textContent)
      .toContain('Já existe outra música com esses dados.');
  });

  it('deve priorizar a mensagem da API em erros de validação', () => {
    atualizarMusica.mockReturnValue(throwError(() =>
      new HttpErrorResponse({
        status: 400,
        error: { message: 'Associação inválida informada pela API.' }
      })
    ));
    criarComponente();

    component.salvar(requestAtualizado);

    expect(component.mensagemErro())
      .toBe('Associação inválida informada pela API.');
  });
});
