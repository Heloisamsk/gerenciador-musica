import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import type { PlaylistResponse } from '../../models/PlaylistResponse';
import { PlaylistCard } from './playlist-card';

describe('PlaylistCard', () => {
  let fixture: ComponentFixture<PlaylistCard>;

  const playlistDeExemplo: PlaylistResponse = {
    id: 7,
    nome: 'Favoritas',
    descricao: 'Minhas músicas preferidas',
    capaUrl: 'https://exemplo.com/capa.jpg',
    musicas: [
      { id: 1, titulo: 'Bohemian Rhapsody', artista: 'Queen', capaUrl: null }
    ],
    especial: false
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlaylistCard],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(PlaylistCard);
    fixture.componentRef.setInput('playlist', playlistDeExemplo);
    fixture.detectChanges();
  });

  it('deve exibir o nome, a contagem de músicas e linkar para a playlist', () => {
    const texto = fixture.nativeElement.textContent;
    const link = fixture.nativeElement.querySelector('a.playlist-card');

    expect(texto).toContain('Favoritas');
    expect(texto).toContain('1 música');
    expect(link.getAttribute('href')).toBe('/playlists/7');
  });

  it('deve exibir a capa quando disponível', () => {
    const imagem = fixture.nativeElement.querySelector(
      '.playlist-card__capa img'
    ) as HTMLImageElement;

    expect(imagem.src).toBe('https://exemplo.com/capa.jpg');
  });

  it('deve exibir uma nota musical quando não há capa', () => {
    fixture.componentRef.setInput('playlist', {
      ...playlistDeExemplo,
      capaUrl: null
    });
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.playlist-card__capa img')
    ).toBeNull();
    expect(
      fixture.nativeElement.querySelector('.playlist-card__nota')
    ).not.toBeNull();
  });

  it('deve trocar para a capa padrão quando a imagem falhar', () => {
    const imagem = fixture.nativeElement.querySelector(
      '.playlist-card__capa img'
    ) as HTMLImageElement;

    imagem.dispatchEvent(new Event('error'));

    expect(imagem.src).toContain('/capa-padrao.png');
  });

  it('deve usar o plural quando houver mais de uma música', () => {
    fixture.componentRef.setInput('playlist', {
      ...playlistDeExemplo,
      musicas: [
        { id: 1, titulo: 'A', artista: 'X', capaUrl: null },
        { id: 2, titulo: 'B', artista: 'Y', capaUrl: null }
      ]
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('2 músicas');
  });
});
