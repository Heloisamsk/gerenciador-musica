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

import type { AlbumDetalhe } from '../../models/AlbumDetalhe';
import { AlbumDetalhePage } from './album-detalhe';

describe('AlbumDetalhePage', () => {
  let component: AlbumDetalhePage;
  let fixture: ComponentFixture<AlbumDetalhePage>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/albuns/10/detalhes';

  afterEach(() => {
    httpMock?.verify();
  });

  it('deve carregar e exibir os detalhes e as faixas do álbum', async () => {
    await configurarComId('10');
    fixture.detectChanges();

    const requisicao = httpMock.expectOne(apiUrl);
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush(detalhesDeExemplo());
    fixture.detectChanges();

    const texto = fixture.nativeElement.textContent;
    const linkMusica = fixture.nativeElement.querySelector(
      'a.linha-faixa'
    ) as HTMLAnchorElement;
    const linkArtista = fixture.nativeElement.querySelector(
      'a.artista-link'
    ) as HTMLAnchorElement;

    expect(component.detalhes()?.album.titulo)
      .toBe('A Night at the Opera');
    expect(component.carregando()).toBe(false);
    expect(texto).toContain('Queen');
    expect(texto).toContain('Bohemian Rhapsody');
    expect(texto).toContain('Rock');
    expect(linkMusica.getAttribute('href')).toBe('/musicas/20');
    expect(linkArtista.getAttribute('href')).toBe('/artistas/1');
  });

  it('não deve consultar a API quando o id for inválido', async () => {
    await configurarComId('abc');
    fixture.detectChanges();

    httpMock.expectNone(
      'http://localhost:8080/api/albuns/NaN/detalhes'
    );
    expect(component.mensagemErro()).toBe(
      'O identificador do álbum é inválido.'
    );
  });

  it.each([
    [400, 'O identificador do álbum é inválido.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não tem permissão para consultar este álbum.'],
    [404, 'Álbum não encontrado.'],
    [500, 'Ocorreu um erro no servidor. Tente novamente mais tarde.'],
    [418, 'Não foi possível carregar o álbum. Tente novamente.']
  ])(
    'deve mapear o erro HTTP %s para uma mensagem segura',
    async (status, mensagem) => {
      await configurarComId('10');
      fixture.detectChanges();

      httpMock.expectOne(apiUrl).flush(
        {},
        { status, statusText: 'Erro' }
      );

      expect(component.mensagemErro()).toBe(mensagem);
      expect(component.carregando()).toBe(false);
    }
  );

  it('deve informar erro de conexão com o servidor', async () => {
    await configurarComId('10');
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).error(new ProgressEvent('network'));

    expect(component.mensagemErro()).toBe(
      'Não foi possível conectar ao servidor. Tente novamente.'
    );
  });

  it('deve tentar carregar novamente depois de uma falha', async () => {
    await configurarComId('10');
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).flush(
      {},
      { status: 500, statusText: 'Internal Server Error' }
    );

    component.tentarNovamente();
    httpMock.expectOne(apiUrl).flush(detalhesDeExemplo());

    expect(component.mensagemErro()).toBe('');
    expect(component.detalhes()?.album.idAlbum).toBe(10);
  });

  it('deve exibir estado vazio para álbum sem faixas', async () => {
    await configurarComId('10');
    fixture.detectChanges();

    const detalhes = detalhesDeExemplo();
    detalhes.album.totalMusicas = 0;
    detalhes.album.duracaoTotalSegundos = 0;
    detalhes.generos = [];
    detalhes.musicas = [];

    httpMock.expectOne(apiUrl).flush(detalhes);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Este álbum ainda não possui músicas cadastradas.'
    );
  });

  it('deve substituir uma capa inválida pela imagem padrão', async () => {
    await configurarComId('10');
    const imagem = document.createElement('img');
    imagem.src = 'https://example.com/inexistente.jpg';

    component.substituirImagem(
      { target: imagem } as unknown as Event,
      '/capa-padrao.png'
    );

    expect(imagem.src).toContain('/capa-padrao.png');
    expect(imagem.onerror).toBeNull();
  });

  async function configurarComId(id: string): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [AlbumDetalhePage],
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

    fixture = TestBed.createComponent(AlbumDetalhePage);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  }

  function detalhesDeExemplo(): AlbumDetalhe {
    return {
      album: {
        idAlbum: 10,
        idArtista: 1,
        nomeArtista: 'Queen',
        titulo: 'A Night at the Opera',
        anoLancamento: 1975,
        capaUrl: null,
        totalMusicas: 1,
        duracaoTotalSegundos: 354
      },
      generos: ['Rock'],
      musicas: [
        {
          idMusica: 20,
          titulo: 'Bohemian Rhapsody',
          duracaoSegundos: 354,
          generos: ['Rock']
        }
      ]
    };
  }
});
