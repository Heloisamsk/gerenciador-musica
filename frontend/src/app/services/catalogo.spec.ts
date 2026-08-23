import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import type { ArtistaDetalhe } from '../models/ArtistaDetalhe';
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
        duracaoTotalSegundos: 354
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
          duracaoTotalSegundos: 354
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
