import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { PlaylistService } from './playlist';
import { PlaylistResponse } from '../models/PlaylistResponse';

/*
 * Teste de UNIDADE do PlaylistService, seguindo o mesmo padrão do
 * AuthServiceTest: o service é real, só o HttpClient é substituído
 * por uma versão de teste (HttpTestingController), então nenhuma
 * chamada de rede de verdade acontece.
 */
describe('PlaylistService', () => {
  let service: PlaylistService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/playlists';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PlaylistService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(PlaylistService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('deve enviar POST para criar uma playlist', () => {
    const novaPlaylist = {
      nome: 'Favoritas',
      descricao: 'Minhas músicas favoritas',
      capaUrl: null,
    };

    service.criar(novaPlaylist).subscribe();

    const requisicao = httpMock.expectOne(apiUrl);

    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual(novaPlaylist);

    requisicao.flush({
      id: 1,
      nome: 'Favoritas',
      descricao: 'Minhas músicas favoritas',
      capaUrl: null,
      musicas: [],
      especial: false,
    } as PlaylistResponse);
  });

  it('deve buscar a lista de playlists do usuário', () => {
    service.listarMinhas().subscribe((playlists) => {
      expect(playlists).toHaveLength(1);
      expect(playlists[0].nome).toBe('Favoritas');
    });

    const requisicao = httpMock.expectOne(apiUrl);

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush([
      { id: 1, nome: 'Favoritas', descricao: '', capaUrl: null, musicas: [], especial: false },
    ] as PlaylistResponse[]);
  });

  it('deve buscar uma playlist por id', () => {
    service.buscarPorId(1).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/1`);

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush({
      id: 1,
      nome: 'Favoritas',
      descricao: '',
      capaUrl: null,
      musicas: [],
      especial: false,
    } as PlaylistResponse);
  });

  it('deve enviar PUT para atualizar uma playlist', () => {
    const dados = {
      nome: 'Favoritas atualizadas',
      descricao: 'Nova descrição',
      capaUrl: 'https://exemplo.com/capa.jpg',
    };

    service.atualizar(1, dados).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/1`);

    expect(requisicao.request.method).toBe('PUT');
    expect(requisicao.request.body).toEqual(dados);

    requisicao.flush({
      id: 1,
      ...dados,
      musicas: [],
      especial: false,
    } as PlaylistResponse);
  });

  it('deve enviar DELETE para excluir uma playlist', () => {
    service.excluir(1).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/1`);

    expect(requisicao.request.method).toBe('DELETE');

    requisicao.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('deve propagar um erro amigável quando a requisição falha', () => {
    let erroCapturado: Error | undefined;

    service.buscarPorId(999).subscribe({
      error: (erro) => (erroCapturado = erro),
    });

    httpMock
      .expectOne(`${apiUrl}/999`)
      .flush('Playlist não encontrada', { status: 404, statusText: 'Not Found' });

    expect(erroCapturado).toBeInstanceOf(Error);
    expect(erroCapturado?.message).toContain('404');
  });

  it('deve propagar a mensagem de um erro de rede', () => {
    let erroCapturado: Error | undefined;
    vi.spyOn(console, 'error').mockImplementation(() => {});

    service.buscarPorId(1).subscribe({
      error: erro => {
        erroCapturado = erro;
      }
    });

    httpMock
      .expectOne(`${apiUrl}/1`)
      .error(
        new ErrorEvent(
          'NetworkError',
          {
            message: 'Falha de conexão'
          }
        )
      );

    expect(erroCapturado).toBeInstanceOf(Error);
    expect(erroCapturado?.message).toBe(
      'Erro: Falha de conexão'
    );
  });

  it('deve enviar POST para adicionar uma música na playlist', () => {
    service.adicionarMusica(1, 5).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/1/musicas/5`);

    expect(requisicao.request.method).toBe('POST');

    requisicao.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('deve enviar DELETE para remover uma música da playlist', () => {
    service.removerMusica(1, 5).subscribe();

    const requisicao = httpMock.expectOne(`${apiUrl}/1/musicas/5`);

    expect(requisicao.request.method).toBe('DELETE');

    requisicao.flush(null, { status: 204, statusText: 'No Content' });
  });
});
