import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';

import type { AlbumResponse } from '../../models/AlbumResponse';
import type { ArtistaResponse } from '../../models/ArtistaResponse';
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

  function ignorarCargaInicial(): void {
    fixture.detectChanges();
    httpMock.expectOne(perfilUrl).flush(perfilDeExemplo());
    httpMock.expectOne(`${apiUrl}/artistas`).flush([]);
    httpMock.expectOne(`${apiUrl}/albuns`).flush([]);
    httpMock.expectOne(playlistsUrl).flush([]);
    httpMock.expectOne(`${apiUrl}/albuns/curtidos`).flush([]);
    httpMock.expectOne(`${apiUrl}/artistas/seguidos`).flush([]);
  }

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

  it('deve substituir a sidebar inteira pela biblioteca, sem subtítulos decorativos', () => {
    carregarCatalogo([]);

    const iconesMenuMobile = fixture.nativeElement.querySelectorAll(
      '.menu-mobile > a svg'
    );

    expect(fixture.nativeElement.querySelector('.sidebar .marca')).toBeNull();
    expect(fixture.nativeElement.querySelector('.sidebar nav.menu')).toBeNull();
    expect(fixture.nativeElement.querySelector('.sidebar .biblioteca')).not.toBeNull();
    expect(iconesMenuMobile.length).toBeGreaterThanOrEqual(5);
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
    httpMock.expectOne(`${apiUrl}/albuns/curtidos`).flush([]);
    httpMock.expectOne(`${apiUrl}/artistas/seguidos`).flush([]);
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
    const homeShell = fixture.nativeElement.querySelector('.home-shell');
    const playlists = fixture.nativeElement.querySelectorAll('.biblioteca-playlist');

    expect(component.bibliotecaExpandida()).toBe(false);
    expect(biblioteca.classList.contains('biblioteca--expandida')).toBe(false);
    expect(homeShell.classList.contains('home-shell--reduzido')).toBe(true);
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
    const homeShell = fixture.nativeElement.querySelector('.home-shell');
    const atalhos = fixture.nativeElement.querySelectorAll(
      '.biblioteca-atalhos a'
    ) as NodeListOf<HTMLAnchorElement>;
    const playlist = fixture.nativeElement.querySelector(
      '.biblioteca-playlist'
    ) as HTMLAnchorElement;

    expect(component.bibliotecaExpandida()).toBe(true);
    expect(biblioteca.classList.contains('biblioteca--expandida')).toBe(true);
    expect(homeShell.classList.contains('home-shell--reduzido')).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Sua Biblioteca');
    expect(atalhos[0].textContent).toContain('Minhas playlists');
    expect(atalhos[0].getAttribute('href')).toBe('/playlists');
    expect(atalhos[1].textContent).toContain('Minhas reviews');
    expect(atalhos[1].getAttribute('href')).toBe('/reviews?escopo=MINHAS');
    expect(playlist.textContent).toContain('Foco no trabalho');
    expect(playlist.getAttribute('href')).toBe('/playlists/7');
  });

  it('deve tratar erro ao carregar perfil, artistas, playlists, álbuns curtidos e artistas seguidos', () => {
    fixture.detectChanges();

    httpMock.expectOne(perfilUrl).flush(
      {}, { status: 500, statusText: 'Internal Server Error' }
    );
    httpMock.expectOne(`${apiUrl}/artistas`).flush(
      {}, { status: 500, statusText: 'Internal Server Error' }
    );
    httpMock.expectOne(`${apiUrl}/albuns`).flush([]);
    httpMock.expectOne(playlistsUrl).flush(
      {}, { status: 500, statusText: 'Internal Server Error' }
    );
    httpMock.expectOne(`${apiUrl}/albuns/curtidos`).flush(
      {}, { status: 500, statusText: 'Internal Server Error' }
    );
    httpMock.expectOne(`${apiUrl}/artistas/seguidos`).flush(
      {}, { status: 500, statusText: 'Internal Server Error' }
    );
    fixture.detectChanges();

    expect(component.fotoPerfil()).toBe('/avatar-padrao.svg');
    expect(component.artistas()).toEqual([]);
    expect(component.erroArtistas()).toBe(
      'Não foi possível carregar os artistas.'
    );
    expect(component.playlists()).toEqual([]);
    expect(component.albunsCurtidos()).toEqual([]);
    expect(component.artistasSeguidos()).toEqual([]);
  });

  it('deve indicar administrador conforme o papel salvo', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(chave =>
      chave === 'role' ? 'ADMIN' : null
    );
    expect(component.isAdmin()).toBe(true);

    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(chave =>
      chave === 'role' ? 'USER' : null
    );
    expect(component.isAdmin()).toBe(false);
  });

  it('deve buscar instantaneamente ao digitar um termo', () => {
    ignorarCargaInicial();
    vi.useFakeTimers();

    component.atualizarBusca(
      { target: { value: '  queen  ' } } as unknown as Event
    );

    expect(component.mostrarResultadosBusca()).toBe(true);
    vi.advanceTimersByTime(300);
    vi.useRealTimers();

    const requisicao = httpMock.expectOne(
      req => req.url === `${apiUrl}/busca`
        && req.params.get('q') === 'queen'
    );
    requisicao.flush({ musicas: [], albuns: [], artistas: [], usuarios: [] });

    expect(component.resultadoBusca()).toEqual({
      musicas: [], albuns: [], artistas: [], usuarios: []
    });
    expect(component.buscandoInstantaneo()).toBe(false);
  });

  it('não deve buscar nem exibir o dropdown quando o termo está vazio', () => {
    ignorarCargaInicial();
    vi.useFakeTimers();

    component.atualizarBusca(
      { target: { value: '   ' } } as unknown as Event
    );
    vi.advanceTimersByTime(300);
    vi.useRealTimers();

    httpMock.expectNone(req => req.url === `${apiUrl}/busca`);
    expect(component.mostrarResultadosBusca()).toBe(false);
    expect(component.resultadoBusca()).toBeNull();
  });

  it('deve descartar o resultado quando a busca instantânea falhar', () => {
    ignorarCargaInicial();
    vi.useFakeTimers();

    component.atualizarBusca(
      { target: { value: 'queen' } } as unknown as Event
    );
    vi.advanceTimersByTime(300);
    vi.useRealTimers();

    httpMock.expectOne(req => req.url === `${apiUrl}/busca`).flush(
      {}, { status: 500, statusText: 'Internal Server Error' }
    );

    expect(component.resultadoBusca()).toBeNull();
    expect(component.buscandoInstantaneo()).toBe(false);
  });

  it('deve navegar para a busca completa informando o título digitado', () => {
    ignorarCargaInicial();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.termoBusca.set('queen');
    component.mostrarResultadosBusca.set(true);
    component.pesquisar();

    expect(navigateSpy).toHaveBeenCalledWith(
      ['/musicas'],
      { queryParams: { titulo: 'queen' } }
    );
    expect(component.mostrarResultadosBusca()).toBe(false);
  });

  it('deve navegar para a busca completa sem título quando o campo está vazio', () => {
    ignorarCargaInicial();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.termoBusca.set('   ');
    component.pesquisar();

    expect(navigateSpy).toHaveBeenCalledWith(
      ['/musicas'],
      { queryParams: {} }
    );
  });

  it('deve reabrir os resultados da busca somente quando há um termo digitado', () => {
    ignorarCargaInicial();

    component.termoBusca.set('');
    component.reabrirResultadosBusca();
    expect(component.mostrarResultadosBusca()).toBe(false);

    component.termoBusca.set('queen');
    component.reabrirResultadosBusca();
    expect(component.mostrarResultadosBusca()).toBe(true);
  });

  it('deve fechar os resultados da busca ao pressionar Escape', () => {
    ignorarCargaInicial();
    component.mostrarResultadosBusca.set(true);

    component.tratarTeclaBusca({ key: 'Escape' } as KeyboardEvent);
    expect(component.mostrarResultadosBusca()).toBe(false);

    component.mostrarResultadosBusca.set(true);
    component.tratarTeclaBusca({ key: 'Enter' } as KeyboardEvent);
    expect(component.mostrarResultadosBusca()).toBe(true);
  });

  it('deve fechar os resultados da busca ao clicar fora da área de busca', () => {
    ignorarCargaInicial();
    component.mostrarResultadosBusca.set(true);

    const foraDaBusca = document.createElement('div');
    component.aoClicarFora({ target: foraDaBusca } as unknown as MouseEvent);
    expect(component.mostrarResultadosBusca()).toBe(false);
  });

  it('não deve fechar os resultados da busca ao clicar dentro dela', () => {
    ignorarCargaInicial();
    component.mostrarResultadosBusca.set(true);

    const wrapper = document.createElement('div');
    wrapper.className = 'busca-wrapper';
    const filho = document.createElement('span');
    wrapper.appendChild(filho);

    component.aoClicarFora({ target: filho } as unknown as MouseEvent);
    expect(component.mostrarResultadosBusca()).toBe(true);
  });

  it('deve encerrar a sessão e navegar para o login', () => {
    ignorarCargaInicial();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.logout();

    httpMock.expectOne(`${apiUrl}/auth/logout`).flush({ mensagem: 'ok' });

    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('deve navegar para o login mesmo quando o logout falha no backend', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    ignorarCargaInicial();
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.logout();

    httpMock.expectOne(`${apiUrl}/auth/logout`).flush(
      {}, { status: 500, statusText: 'Internal Server Error' }
    );

    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('deve substituir a imagem do usuário e do artista por padrão em caso de erro', () => {
    ignorarCargaInicial();

    const imagemPerfil = document.createElement('img');
    imagemPerfil.onerror = () => {};
    component.substituirImagem(
      { target: imagemPerfil } as unknown as Event,
      '/avatar-padrao.svg'
    );
    expect(imagemPerfil.onerror).toBeNull();
    expect(imagemPerfil.src).toContain('/avatar-padrao.svg');

    const imagemArtista = document.createElement('img');
    imagemArtista.onerror = () => {};
    component.substituirFotoArtista(
      { target: imagemArtista } as unknown as Event
    );
    expect(imagemArtista.onerror).toBeNull();
    expect(imagemArtista.src).toContain('/avatar-artista.png');
  });

  function playlistDeExemplo(): PlaylistResponse {
    return {
      id: 7,
      nome: 'Foco no trabalho',
      descricao: '',
      capaUrl: 'https://exemplo.com/capa-playlist.jpg',
      musicas: [],
      especial: false
    };
  }

  function carregarCatalogo(
    albuns: AlbumResponse[],
    perfil: PerfilResponse = perfilDeExemplo(),
    playlists: PlaylistResponse[] = [],
    albunsCurtidos: AlbumResponse[] = [],
    artistasSeguidos: ArtistaResponse[] = []
  ): void {
    fixture.detectChanges();

    httpMock.expectOne(perfilUrl).flush(perfil);
    httpMock.expectOne(`${apiUrl}/artistas`).flush([]);
    httpMock.expectOne(`${apiUrl}/albuns`).flush(albuns);
    httpMock.expectOne(playlistsUrl).flush(playlists);
    httpMock.expectOne(`${apiUrl}/albuns/curtidos`).flush(albunsCurtidos);
    httpMock.expectOne(`${apiUrl}/artistas/seguidos`).flush(artistasSeguidos);
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
      },
      curtida: false
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
