import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  provideRouter
} from '@angular/router';

import type { ArtistaDetalhe } from '../../models/ArtistaDetalhe';
import { ArtistaDetalhePage } from './artista-detalhe';

describe('ArtistaDetalhePage', () => {
  let component: ArtistaDetalhePage;
  let fixture: ComponentFixture<ArtistaDetalhePage>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/artistas/1/detalhes';

  afterEach(() => {
    httpMock?.verify();
  });

  it('deve carregar dados das três views e exibi-los na página', async () => {
    await configurarComId('1');
    fixture.detectChanges();

    const requisicao = httpMock.expectOne(apiUrl);
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush(detalhesDeExemplo());
    fixture.detectChanges();

    const texto = fixture.nativeElement.textContent;

    expect(component.detalhes()?.artista.nome).toBe('Queen');
    expect(component.carregando()).toBe(false);
    expect(texto).toContain('A Night at the Opera');
    expect(texto).toContain('Bohemian Rhapsody');
    expect(texto).toContain('Artista principal');

    const linkAlbum = fixture.nativeElement.querySelector(
      'a.link-album'
    ) as HTMLAnchorElement;
    expect(linkAlbum.getAttribute('href')).toBe('/albuns/10');
  });

  it('não deve chamar a API quando o id da rota for inválido', async () => {
    await configurarComId('abc');
    fixture.detectChanges();

    httpMock.expectNone(
      'http://localhost:8080/api/artistas/NaN/detalhes'
    );
    expect(component.mensagemErro()).toBe(
      'O identificador do artista é inválido.'
    );
  });

  it('deve informar quando o artista não for encontrado', async () => {
    await configurarComId('1');
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).flush(
      { message: 'Artista não encontrado' },
      { status: 404, statusText: 'Not Found' }
    );

    expect(component.mensagemErro()).toBe('Artista não encontrado.');
    expect(component.carregando()).toBe(false);
  });

  it('deve permitir tentar novamente depois de uma falha', async () => {
    await configurarComId('1');
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).flush(
      {},
      { status: 500, statusText: 'Internal Server Error' }
    );

    component.tentarNovamente();
    httpMock.expectOne(apiUrl).flush(detalhesDeExemplo());

    expect(component.detalhes()?.artista.nome).toBe('Queen');
    expect(component.mensagemErro()).toBe('');
  });

  it('deve exibir estados vazios para artista sem catálogo', async () => {
    await configurarComId('1');
    fixture.detectChanges();

    const detalhes = detalhesDeExemplo();
    detalhes.artista.totalAlbuns = 0;
    detalhes.artista.totalMusicasPrincipais = 0;
    detalhes.albuns = [];
    detalhes.musicas = [];

    httpMock.expectOne(apiUrl).flush(detalhes);
    fixture.detectChanges();

    const texto = fixture.nativeElement.textContent;

    expect(texto).toContain(
      'Este artista ainda não possui álbuns cadastrados.'
    );
    expect(texto).toContain(
      'Este artista ainda não possui músicas relacionadas.'
    );
  });

  async function configurarComId(id: string): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [ArtistaDetalhePage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id })
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ArtistaDetalhePage);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  }

  function detalhesDeExemplo(): ArtistaDetalhe {
    return {
      artista: {
        idArtista: 1,
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
          idArtista: 1,
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
          idArtistaPrincipal: 1,
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
