import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  HttpErrorResponse,
  provideHttpClient
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { throwError } from 'rxjs';

import { Playlists } from './playlists';
import { PlaylistResponse } from '../../models/PlaylistResponse';
import { PlaylistService } from '../../services/playlist';

describe('Playlists', () => {
  let component: Playlists;
  let fixture: ComponentFixture<Playlists>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/playlists';
  const albunsCurtidosUrl = 'http://localhost:8080/api/albuns/curtidos';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Playlists],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Playlists);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });

  it('deve carregar as playlists do usuário ao iniciar', () => {
    // A primeira chamada de detectChanges() dispara o ngOnInit.
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).flush([
      { id: 1, nome: 'Favoritas', descricao: '', capaUrl: null, musicas: [], especial: false },
    ] as PlaylistResponse[]);
    httpMock.expectOne(albunsCurtidosUrl).flush([]);

    expect(component.playlists).toHaveLength(1);
    expect(component.playlists[0].nome).toBe('Favoritas');
    expect(component.carregando).toBe(false);
  });

  it('deve exibir a capa da playlist quando disponível e trocar por padrão em caso de erro', () => {
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).flush([
      {
        id: 1,
        nome: 'Favoritas',
        descricao: '',
        capaUrl: 'https://exemplo.com/capa.jpg',
        musicas: [],
        especial: false
      },
    ] as PlaylistResponse[]);
    httpMock.expectOne(albunsCurtidosUrl).flush([]);
    fixture.detectChanges();

    const imagem = fixture.nativeElement.querySelector(
      '.playlist-card__capa img'
    ) as HTMLImageElement;
    expect(imagem.src).toBe('https://exemplo.com/capa.jpg');

    imagem.dispatchEvent(new Event('error'));
    expect(imagem.src).toContain('/capa-padrao.png');
  });

  it('deve mostrar mensagem de erro quando a busca falha', () => {
    fixture.detectChanges();

    httpMock
      .expectOne(apiUrl)
      .flush({ message: 'erro' }, { status: 500, statusText: 'Internal Server Error' });
    httpMock.expectOne(albunsCurtidosUrl).flush([]);

    expect(component.mensagemErro).toBeTruthy();
    expect(component.carregando).toBe(false);
  });

  const cenariosDeAutorizacao = [
    {
      status: 401,
      statusText: 'Unauthorized',
      mensagemEsperada:
        'Sua sessão expirou. Faça login novamente.'
    },
    {
      status: 403,
      statusText: 'Forbidden',
      mensagemEsperada:
        'Você não tem permissão para ver essas playlists.'
    }
  ];

  for (const cenario of cenariosDeAutorizacao) {
    it(`deve tratar erro ${cenario.status} ao listar playlists`, () => {
      vi.spyOn(console, 'error').mockImplementation(() => {});
      const playlistService = TestBed.inject(PlaylistService);

      vi.spyOn(playlistService, 'listarMinhas').mockReturnValue(
        throwError(
          () => new HttpErrorResponse({
            status: cenario.status,
            statusText: cenario.statusText
          })
        )
      );

      fixture.detectChanges();

      httpMock.expectNone(apiUrl);
      httpMock.expectOne(albunsCurtidosUrl).flush([]);

      expect(component.mensagemErro).toBe(
        cenario.mensagemEsperada
      );
      expect(component.carregando).toBe(false);
    });
  }
});
