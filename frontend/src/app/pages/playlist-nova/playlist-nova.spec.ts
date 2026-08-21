import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  HttpErrorResponse,
  provideHttpClient
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { throwError } from 'rxjs';

import { PlaylistNova } from './playlist-nova';
import { PlaylistService } from '../../services/playlist';

describe('PlaylistNova', () => {
  let component: PlaylistNova;
  let fixture: ComponentFixture<PlaylistNova>;
  let httpMock: HttpTestingController;
  let router: Router;

  const apiUrl = 'http://localhost:8080/api/playlists';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaylistNova],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PlaylistNova);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('não deve enviar requisição quando o formulário é inválido', () => {
    component.formulario.setValue({ nome: '', descricao: '' });

    component.enviar();

    httpMock.expectNone(apiUrl);
  });

  it('deve navegar para a playlist criada quando o envio tem sucesso', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.formulario.setValue({ nome: 'Favoritas', descricao: 'Minhas músicas' });

    component.enviar();

    httpMock.expectOne(apiUrl).flush({
      id: 7,
      nome: 'Favoritas',
      descricao: 'Minhas músicas',
      musicas: [],
    });

    expect(navigateSpy).toHaveBeenCalledWith(['/playlists', 7]);
    expect(component.enviando).toBe(false);
  });

  it('deve mostrar mensagem de erro quando o envio falha', () => {
    component.formulario.setValue({ nome: 'Favoritas', descricao: '' });

    component.enviar();

    httpMock
      .expectOne(apiUrl)
      .flush({ message: 'erro' }, { status: 500, statusText: 'Internal Server Error' });

    expect(component.mensagemErro).toBeTruthy();
    expect(component.enviando).toBe(false);
  });

  it('deve informar quando a sessão expirar ao criar playlist', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    const playlistService = TestBed.inject(PlaylistService);

    vi.spyOn(playlistService, 'criar').mockReturnValue(
      throwError(
        () => new HttpErrorResponse({
          status: 401,
          statusText: 'Unauthorized'
        })
      )
    );

    component.formulario.setValue({
      nome: 'Favoritas',
      descricao: ''
    });

    component.enviar();

    httpMock.expectNone(apiUrl);

    expect(component.mensagemErro).toBe(
      'Sua sessão expirou. Faça login novamente.'
    );
    expect(component.enviando).toBe(false);
  });
});
