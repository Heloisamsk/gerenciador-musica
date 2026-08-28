import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import type { AlbumResponse } from '../../models/AlbumResponse';
import { Home } from './home';

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api';

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

  it('deve informar quando não for possível carregar os álbuns', () => {
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}/artistas`).flush([]);
    httpMock.expectOne(`${apiUrl}/albuns`).flush(
      {},
      { status: 500, statusText: 'Internal Server Error' }
    );
    fixture.detectChanges();

    expect(component.albuns()).toEqual([]);
    expect(component.erroAlbuns()).toBe(
      'Não foi possível carregar os álbuns.'
    );
    expect(fixture.nativeElement.textContent).toContain(
      'Não foi possível carregar os álbuns.'
    );
  });

  function carregarCatalogo(albuns: AlbumResponse[]): void {
    fixture.detectChanges();

    httpMock.expectOne(`${apiUrl}/artistas`).flush([]);
    httpMock.expectOne(`${apiUrl}/albuns`).flush(albuns);
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
});
