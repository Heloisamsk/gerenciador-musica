import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Router, provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import type { MusicaRequest } from '../../models/MusicaRequest';
import type { MusicaResponse } from '../../models/MusicaResponse';
import { AdminAlbumService } from '../../services/admin-album.service';
import { AdminArtistaService } from '../../services/admin-artista';
import { AdminMusicaService } from '../../services/admin-musica';
import { FormularioMusica } from '../admin-musicas/formulario-musica/formulario-musica';
import { AdminMusicaNova } from './admin-musica-nova';

describe('AdminMusicaNova', () => {
  let component: AdminMusicaNova;
  let fixture: ComponentFixture<AdminMusicaNova>;
  let router: Router;

  const cadastrarMusica = vi.fn();
  const listarArtistas = vi.fn();
  const listarAlbunsPorArtista = vi.fn();

  const request: MusicaRequest = {
    titulo: 'Música de Teste',
    letra: null,
    duracaoSegundos: 180,
    anoLancamento: 2025,
    artistaPrincipalId: 7,
    artistasParticipantesIds: [],
    albumId: null,
    generos: ['Gênero de Teste']
  };

  const response: MusicaResponse = {
    id: 31,
    titulo: request.titulo,
    letra: null,
    duracaoSegundos: request.duracaoSegundos,
    anoLancamento: request.anoLancamento,
    artistaPrincipal: { id: 7, nome: 'Artista de Teste' },
    album: null,
    artistasParticipantes: [],
    generos: [{ id: 1, nome: request.generos[0] }]
  };

  beforeEach(async () => {
    cadastrarMusica.mockReset();
    listarArtistas.mockReset();
    listarAlbunsPorArtista.mockReset();
    cadastrarMusica.mockReturnValue(of(response));
    listarArtistas.mockReturnValue(of([
      {
        idArtista: 7,
        nome: 'Artista de Teste',
        nomeCompleto: 'Nome do Artista de Teste',
        descricao: 'Descrição de teste.',
        fotoPerfilUrl: null
      }
    ]));
    listarAlbunsPorArtista.mockReturnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [AdminMusicaNova],
      providers: [
        provideRouter([]),
        {
          provide: AdminMusicaService,
          useValue: { cadastrarMusica }
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
    fixture = TestBed.createComponent(AdminMusicaNova);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function obterFormulario(): FormularioMusica {
    return fixture.debugElement.query(
      By.directive(FormularioMusica)
    ).componentInstance as FormularioMusica;
  }

  it('deve reutilizar o formulário no modo de cadastro', () => {
    const formulario = obterFormulario();

    expect(formulario.modo()).toBe('cadastro');
    expect(formulario.exibirCancelar()).toBe(false);
  });

  it('deve cadastrar e redirecionar com mensagem de sucesso', () => {
    component.salvar(request);

    expect(cadastrarMusica).toHaveBeenCalledWith(request);
    expect(component.mensagemSucesso())
      .toBe('Música Música de Teste cadastrada com sucesso!');
    expect(router.navigate).toHaveBeenCalledWith(
      ['/admin/banco/musicas'],
      {
        state: {
          mensagemSucesso:
            'Música Música de Teste cadastrada com sucesso!'
        }
      }
    );
  });

  it('deve bloquear envios duplicados durante o cadastro', () => {
    const resposta = new Subject<MusicaResponse>();
    cadastrarMusica.mockReturnValue(resposta.asObservable());

    component.salvar(request);
    component.salvar(request);

    expect(cadastrarMusica).toHaveBeenCalledOnce();
    expect(component.salvando()).toBe(true);

    resposta.complete();
    expect(component.salvando()).toBe(false);
  });

  it.each([
    [0, 'Não foi possível conectar ao servidor.'],
    [400, 'Existem dados inválidos no formulário.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não possui permissão para cadastrar músicas.'],
    [404, 'Um dos dados associados não foi encontrado.'],
    [409, 'Esta música já está cadastrada.'],
    [500, 'Ocorreu um erro no servidor ao cadastrar a música.'],
    [418, 'Não foi possível cadastrar a música.']
  ])('deve tratar o erro HTTP %i', (status, mensagem) => {
    cadastrarMusica.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status })
    ));

    component.salvar(request);

    expect(component.mensagemErro()).toBe(mensagem);
    expect(component.salvando()).toBe(false);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('deve priorizar a mensagem da API para erros de negócio', () => {
    cadastrarMusica.mockReturnValue(throwError(() =>
      new HttpErrorResponse({
        status: 409,
        error: { message: 'Conflito informado pela API.' }
      })
    ));

    component.salvar(request);

    expect(component.mensagemErro())
      .toBe('Conflito informado pela API.');
  });
});
