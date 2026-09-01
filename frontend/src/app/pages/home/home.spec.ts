import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';

import type { AlbumResponse } from '../../models/AlbumResponse';
import type { PerfilResponse } from '../../models/Perfil';
import type { PlaylistResponse } from '../../models/PlaylistResponse';
import { Home } from './home';

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api';
  const perfilUrl = `${apiUrl}/user/perfil`;
  const playlistsUrl = `${apiUrl}/playlists`;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    vi.restoreAllMocks();
  });

  it('deve criar e carregar artistas e álbuns', () => {
    carregarCatalogo([albumDeExemplo()]);

    expect(component).toBeTruthy();
    expect(component.albuns()).toHaveLength(1);
  });

  it('deve exibir álbuns clicáveis no lugar das músicas', () => {
    carregarCatalogo([albumDeExemplo()]);

    const tituloSecao = fixture.nativeElement.querySelectorAll('h2')[1];
    const linkAlbum = fixture.nativeElement.querySelector(
      'a.album-card'
    ) as HTMLAnchorElement;

    expect(tituloSecao.textContent).toContain('Álbuns');
    expect(linkAlbum.textContent).toContain('A Night at the Opera');
    expect(linkAlbum.textContent).toContain('Queen');
    expect(linkAlbum.getAttribute('href')).toBe('/albuns/10');
    expect(fixture.nativeElement.querySelector('.musica-card')).toBeNull();
  });

  it('deve exibir a identidade Crotchet sem subtítulos decorativos', () => {
    carregarCatalogo([]);

    const marca = fixture.nativeElement.querySelector('.marca');
    const iconesMenu = fixture.nativeElement.querySelectorAll(
      '.menu-link .menu-nota'
    );

    expect(marca.textContent).toContain('Crotchet - Music Hub');
    expect(marca.querySelector('svg')).not.toBeNull();
    expect(iconesMenu.length).toBeGreaterThanOrEqual(5);
    expect(fixture.nativeElement.querySelector('.hero-sobretitulo')).toBeNull();
    expect(fixture.nativeElement.querySelector('.secao-sobretitulo')).toBeNull();
  });

  it('deve usar no cabeçalho a foto escolhida no perfil', () => {
    carregarCatalogo([], {
      ...perfilDeExemplo(),
      fotoUrl: 'https://exemplo.com/minha-foto.jpg'
    });

    const foto = fixture.nativeElement.querySelector(
      '.topo-perfil img'
    ) as HTMLImageElement;

    expect(foto.src).toBe('https://exemplo.com/minha-foto.jpg');
    expect(fixture.nativeElement.querySelector('.sidebar-rodape')).toBeNull();
    expect(fixture.nativeElement.querySelector('.topo-sair')).not.toBeNull();
  });

  it('deve sortear o álbum exibido em Para começar', () => {
    const geradorSeguro = vi.spyOn(globalThis.crypto, 'getRandomValues');
    const albuns = [
      albumDeExemplo(),
      { ...albumDeExemplo(), idAlbum: 11, titulo: 'Jazz' }
    ];

    carregarCatalogo(albuns);

    expect(geradorSeguro).toHaveBeenCalledOnce();
    expect(albuns.map(album => album.idAlbum))
      .toContain(component.albumDestaque()?.idAlbum);
  });

  it('deve informar quando não for possível carregar os álbuns', () => {
    fixture.detectChanges();

    httpMock.expectOne(perfilUrl).flush(perfilDeExemplo());
    httpMock.expectOne(`${apiUrl}/artistas`).flush([]);
    httpMock.expectOne(`${apiUrl}/albuns`).flush(
      {},
      { status: 500, statusText: 'Internal Server Error' }
    );
    httpMock.expectOne(playlistsUrl).flush([]);
    fixture.detectChanges();

    expect(component.albuns()).toEqual([]);
    expect(component.erroAlbuns()).toBe(
      'Não foi possível carregar os álbuns.'
    );
    expect(fixture.nativeElement.textContent).toContain(
      'Não foi possível carregar os álbuns.'
    );
  });

  it('deve exibir a biblioteca recolhida por padrão, listando só as capas das playlists', () => {
    carregarCatalogo([], perfilDeExemplo(), [playlistDeExemplo()]);

    const biblioteca = fixture.nativeElement.querySelector('.biblioteca');
    const playlists = fixture.nativeElement.querySelectorAll('.biblioteca-playlist');

    expect(component.bibliotecaExpandida()).toBe(false);
    expect(biblioteca.classList.contains('biblioteca--expandida')).toBe(false);
    expect(playlists).toHaveLength(1);
    expect(fixture.nativeElement.querySelector('.biblioteca-playlist-texto')).toBeNull();
    expect(fixture.nativeElement.querySelector('.biblioteca-atalhos')).toBeNull();
  });

  it('deve expandir a biblioteca ao clicar no alternador e mostrar atalhos e playlists', () => {
    carregarCatalogo([], perfilDeExemplo(), [playlistDeExemplo()]);

    const alternador = fixture.nativeElement.querySelector(
      '.biblioteca-alternar'
    ) as HTMLButtonElement;
    alternador.click();
    fixture.detectChanges();

    const biblioteca = fixture.nativeElement.querySelector('.biblioteca');
    const atalhos = fixture.nativeElement.querySelectorAll(
      '.biblioteca-atalhos a'
    ) as NodeListOf<HTMLAnchorElement>;
    const playlist = fixture.nativeElement.querySelector(
      '.biblioteca-playlist'
    ) as HTMLAnchorElement;

    expect(component.bibliotecaExpandida()).toBe(true);
    expect(biblioteca.classList.contains('biblioteca--expandida')).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Sua Biblioteca');
    expect(atalhos[0].textContent).toContain('Minhas playlists');
    expect(atalhos[0].getAttribute('href')).toBe('/playlists');
    expect(atalhos[1].textContent).toContain('Minhas reviews');
    expect(atalhos[1].getAttribute('href')).toBe('/reviews?escopo=MINHAS');
    expect(playlist.textContent).toContain('Foco no trabalho');
    expect(playlist.getAttribute('href')).toBe('/playlists/7');
  });

  function playlistDeExemplo(): PlaylistResponse {
    return {
      id: 7,
      nome: 'Foco no trabalho',
      descricao: '',
      capaUrl: 'https://exemplo.com/capa-playlist.jpg',
      musicas: []
    };
  }

  function carregarCatalogo(
    albuns: AlbumResponse[],
    perfil: PerfilResponse = perfilDeExemplo(),
    playlists: PlaylistResponse[] = []
  ): void {
    fixture.detectChanges();

    httpMock.expectOne(perfilUrl).flush(perfil);
    httpMock.expectOne(`${apiUrl}/artistas`).flush([]);
    httpMock.expectOne(`${apiUrl}/albuns`).flush(albuns);
    httpMock.expectOne(playlistsUrl).flush(playlists);
    fixture.detectChanges();
  }

  function albumDeExemplo(): AlbumResponse {
    return {
      idAlbum: 10,
      titulo: 'A Night at the Opera',
      anoLancamento: 1975,
      capaUrl: null,
      artista: {
        id: 1,
        nome: 'Queen',
        nomeCompleto: 'Queen',
        descricao: 'Banda britânica de rock.',
        fotoPerfilUrl: null
      }
    };
  }

  function perfilDeExemplo(): PerfilResponse {
    return {
      idUsuario: 1,
      username: 'alvaro',
      nome: 'Álvaro',
      dataCadastro: '2026-01-01T12:00:00Z',
      role: 'USER',
      fotoUrl: null,
      bannerUrl: null,
      biografia: null,
      fraseDestaque: null,
      tipoDestaquePrincipal: null,
      artistaDestaque: null,
      musicaDestaque: null,
      albumDestaque: null,
      artistasFavoritos: [],
      albunsFavoritos: [],
      musicasFavoritas: [],
      totalMusicasAvaliadas: 0,
      totalAlbunsAvaliadas: 0
    };
  }
});
