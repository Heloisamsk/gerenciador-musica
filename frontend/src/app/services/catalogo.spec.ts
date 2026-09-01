import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import type { AlbumCapaPublica } from '../models/AlbumCapaPublica';
import type { AlbumDetalhe } from '../models/AlbumDetalhe';
import type { AlbumResponse } from '../models/AlbumResponse';
import type { ArtistaDetalhe } from '../models/ArtistaDetalhe';
import type { ArtistaResponse } from '../models/ArtistaResponse';
import { CatalogoService } from './catalogo';

describe('CatalogoService', () => {
  let service: CatalogoService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CatalogoService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(CatalogoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve buscar os detalhes agregados do artista pelo id', () => {
    let resultado: ArtistaDetalhe | undefined;

    service.buscarDetalhesArtista(7).subscribe(
      detalhes => resultado = detalhes
    );

    const requisicao = httpMock.expectOne(
      `${apiUrl}/artistas/7/detalhes`
    );

    expect(requisicao.request.method).toBe('GET');

    requisicao.flush(detalhesDeExemplo());

    expect(resultado?.artista.nome).toBe('Queen');
    expect(resultado?.albuns).toHaveLength(1);
    expect(resultado?.musicas).toHaveLength(1);
  });

  it('deve listar os álbuns do catálogo', () => {
    let resultado: AlbumResponse[] = [];

    service.listarAlbuns().subscribe(albuns => resultado = albuns);

    const requisicao = httpMock.expectOne(`${apiUrl}/albuns`);
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush([albumDeExemplo()]);

    expect(resultado).toHaveLength(1);
    expect(resultado[0].titulo).toBe('A Night at the Opera');
  });

  it('deve listar os artistas do catálogo', () => {
    let resultado: ArtistaResponse[] = [];

    service.listarArtistas().subscribe(artistas => resultado = artistas);

    const requisicao = httpMock.expectOne(`${apiUrl}/artistas`);
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush([artistaDeExemplo()]);

    expect(resultado).toHaveLength(1);
    expect(resultado[0].nome).toBe('Queen');
  });

  it('deve listar as capas públicas sem autenticação', () => {
    let resultado: AlbumCapaPublica[] = [];

    service.listarCapasPublicas().subscribe(
      capas => resultado = capas
    );

    const requisicao = httpMock.expectOne(
      `${apiUrl}/public/albuns/capas`
    );

    expect(requisicao.request.method).toBe('GET');
    requisicao.flush([capaPublicaDeExemplo()]);

    expect(resultado).toHaveLength(1);
    expect(resultado[0].titulo).toBe('A Night at the Opera');
  });

  it('deve buscar os detalhes agregados do álbum pelo id', () => {
    let resultado: AlbumDetalhe | undefined;

    service.buscarDetalhesAlbum(10).subscribe(
      detalhes => resultado = detalhes
    );

    const requisicao = httpMock.expectOne(
      `${apiUrl}/albuns/10/detalhes`
    );

    expect(requisicao.request.method).toBe('GET');
    requisicao.flush(detalhesAlbumDeExemplo());

    expect(resultado?.album.titulo).toBe('A Night at the Opera');
    expect(resultado?.generos).toEqual(['Rock']);
    expect(resultado?.musicas).toHaveLength(1);
  });

  function albumDeExemplo(): AlbumResponse {
    return {
      idAlbum: 10,
      titulo: 'A Night at the Opera',
      anoLancamento: 1975,
      capaUrl: null,
      artista: {
        id: 7,
        nome: 'Queen',
        nomeCompleto: 'Queen',
        descricao: 'Banda britânica de rock.',
        fotoPerfilUrl: null
      },
      curtida: false
    };
  }

  function capaPublicaDeExemplo(): AlbumCapaPublica {
    return {
      id: 10,
      titulo: 'A Night at the Opera',
      capaUrl: 'https://exemplo.com/capas/10.jpg'
    };
  }

  function artistaDeExemplo(): ArtistaResponse {
    return {
      idArtista: 7,
      nome: 'Queen',
      nomeCompleto: 'Queen',
      descricao: 'Banda britânica de rock.',
      fotoPerfilUrl: null
    };
  }

  function detalhesAlbumDeExemplo(): AlbumDetalhe {
    return {
      album: {
        idAlbum: 10,
        idArtista: 7,
        nomeArtista: 'Queen',
        titulo: 'A Night at the Opera',
        anoLancamento: 1975,
        capaUrl: null,
        totalMusicas: 1,
        duracaoTotalSegundos: 354,
        curtida: false
      },
      generos: ['Rock'],
      musicas: [
        {
          idMusica: 20,
          titulo: 'Bohemian Rhapsody',
          duracaoSegundos: 354,
          generos: ['Rock'],
          curtida: false
        }
      ]
    };
  }

  function detalhesDeExemplo(): ArtistaDetalhe {
    return {
      artista: {
        idArtista: 7,
        nome: 'Queen',
        nomeCompleto: 'Queen',
        descricao: 'Banda britânica de rock.',
        fotoPerfilUrl: null,
        totalAlbuns: 1,
        totalMusicasPrincipais: 1,
        totalParticipacoes: 0,
        duracaoTotalSegundos: 354,
        seguindo: false
      },
      albuns: [
        {
          idAlbum: 10,
          idArtista: 7,
          nomeArtista: 'Queen',
          titulo: 'A Night at the Opera',
          anoLancamento: 1975,
          capaUrl: null,
          totalMusicas: 1,
          duracaoTotalSegundos: 354,
          curtida: false
        }
      ],
      musicas: [
        {
          idMusica: 20,
          titulo: 'Bohemian Rhapsody',
          duracaoSegundos: 354,
          anoLancamento: 1975,
          idArtistaPrincipal: 7,
          nomeArtistaPrincipal: 'Queen',
          idAlbum: 10,
          tituloAlbum: 'A Night at the Opera',
          capaUrl: null,
          generos: ['Rock'],
          papelArtista: 'PRINCIPAL'
        }
      ]
    };
  }
});
