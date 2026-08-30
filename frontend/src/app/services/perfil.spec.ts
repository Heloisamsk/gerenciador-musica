import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import type { AtualizarPerfilRequest, PerfilResponse } from '../models/Perfil';
import { PerfilService } from './perfil';

describe('PerfilService', () => {
  let service: PerfilService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/api/user/perfil';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PerfilService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(PerfilService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve obter o perfil do usuário autenticado', () => {
    let resultado: PerfilResponse | undefined;
    service.obter().subscribe(perfil => resultado = perfil);

    const requisicao = httpMock.expectOne(apiUrl);
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush(perfilExemplo());

    expect(resultado?.nome).toBe('Ana Liz');
    expect(resultado?.artistaDestaque?.titulo).toBe('Marina Sena');
  });

  it('deve enviar a curadoria atualizada do perfil', () => {
    const dados: AtualizarPerfilRequest = {
      nome: 'Ana Liz',
      username: 'analiz',
      fotoUrl: 'https://exemplo.com/foto.jpg',
      bannerUrl: 'https://exemplo.com/banner.jpg',
      biografia: 'Música brasileira e pop.',
      fraseDestaque: 'Som para todos os momentos.',
      idArtistaDestaque: 5,
      idMusicaDestaque: null,
      idAlbumDestaque: null,
      tipoDestaquePrincipal: 'ARTISTA',
      idsArtistasFavoritos: [8],
      idsAlbunsFavoritos: [],
      idsMusicasFavoritas: []
    };

    service.atualizar(dados).subscribe();
    const requisicao = httpMock.expectOne(apiUrl);

    expect(requisicao.request.method).toBe('PUT');
    expect(requisicao.request.body).toEqual(dados);
    requisicao.flush(perfilExemplo());
  });

  function perfilExemplo(): PerfilResponse {
    return {
      idUsuario: 1,
      username: 'analiz',
      nome: 'Ana Liz',
      dataCadastro: '2026-01-01T12:00:00Z',
      role: 'USER',
      fotoUrl: null,
      bannerUrl: null,
      biografia: null,
      fraseDestaque: null,
      tipoDestaquePrincipal: 'ARTISTA',
      artistaDestaque: {
        tipo: 'ARTISTA',
        id: 5,
        titulo: 'Marina Sena',
        subtitulo: 'Artista',
        imagemUrl: null
      },
      musicaDestaque: null,
      albumDestaque: null,
      artistasFavoritos: [],
      albunsFavoritos: [],
      musicasFavoritas: []
    };
  }
});
