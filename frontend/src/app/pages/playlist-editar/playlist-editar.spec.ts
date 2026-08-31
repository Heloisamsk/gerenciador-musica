import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  HttpErrorResponse,
  provideHttpClient
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { throwError } from 'rxjs';

import { PlaylistEditar } from './playlist-editar';
import { PlaylistService } from '../../services/playlist';

describe('PlaylistEditar', () => {
  let component: PlaylistEditar;
  let fixture: ComponentFixture<PlaylistEditar>;
  let httpMock: HttpTestingController;
  let router: Router;

  const apiUrl = 'http://localhost:8080/api/playlists/1';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaylistEditar],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: convertToParamMap({ id: '1' }) },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PlaylistEditar);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });

  function playlistDeExemplo() {
    return {
      id: 1,
      nome: 'Favoritas',
      descricao: 'Minhas músicas',
      capaUrl: 'https://exemplo.com/capa.jpg',
      musicas: [],
    };
  }

  it('deve pré-preencher o formulário com os dados da playlist', () => {
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).flush(playlistDeExemplo());

    expect(component.formulario.value).toEqual({
      nome: 'Favoritas',
      descricao: 'Minhas músicas',
      capaUrl: 'https://exemplo.com/capa.jpg',
    });
  });

  it('deve enviar PUT ao salvar e navegar para a playlist', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture.detectChanges();
    httpMock.expectOne(apiUrl).flush(playlistDeExemplo());

    component.formulario.patchValue({ nome: 'Favoritas atualizadas' });
    component.enviar();

    const requisicao = httpMock.expectOne(apiUrl);
    expect(requisicao.request.method).toBe('PUT');
    expect(requisicao.request.body.nome).toBe('Favoritas atualizadas');

    requisicao.flush({ ...playlistDeExemplo(), nome: 'Favoritas atualizadas' });

    expect(navigateSpy).toHaveBeenCalledWith(['/playlists', 1]);
  });

  it('não deve enviar quando o nome está vazio', () => {
    fixture.detectChanges();
    httpMock.expectOne(apiUrl).flush(playlistDeExemplo());

    component.formulario.patchValue({ nome: '' });
    component.enviar();

    httpMock.expectNone(apiUrl);
  });

  it('deve excluir a playlist quando confirmado e navegar para a listagem', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture.detectChanges();
    httpMock.expectOne(apiUrl).flush(playlistDeExemplo());

    component.excluir();

    const requisicao = httpMock.expectOne(apiUrl);
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null, { status: 204, statusText: 'No Content' });

    expect(navigateSpy).toHaveBeenCalledWith(['/playlists']);
  });

  it('não deve excluir quando o usuário cancela a confirmação', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    fixture.detectChanges();
    httpMock.expectOne(apiUrl).flush(playlistDeExemplo());

    component.excluir();

    httpMock.expectNone(apiUrl);
  });

  it('deve mostrar mensagem específica quando a playlist é de outro usuário', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    const playlistService = TestBed.inject(PlaylistService);

    vi.spyOn(playlistService, 'buscarPorId').mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 403, statusText: 'Forbidden' })
      )
    );

    fixture.detectChanges();

    httpMock.expectNone(apiUrl);
    expect(component.mensagemErro).toBe(
      'Você não tem permissão para alterar esta playlist.'
    );
  });
});
