import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AlbumBackdrop } from './album-backdrop';

describe('AlbumBackdrop', () => {
  let fixture: ComponentFixture<AlbumBackdrop>;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/public/albuns/capas';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlbumBackdrop],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AlbumBackdrop);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve exibir as capas retornadas pela API pública', () => {
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).flush([
      { id: 1, titulo: 'Album 1', capaUrl: '/capa-1.jpg' },
      { id: 2, titulo: 'Album 2', capaUrl: '/capa-2.jpg' }
    ]);
    fixture.detectChanges();

    const imagens = fixture.nativeElement.querySelectorAll(
      '.album-backdrop__capa'
    );

    expect(imagens.length).toBe(2);
  });

  it('não deve quebrar quando a API pública falhar', () => {
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).error(
      new ProgressEvent('erro de rede')
    );
    fixture.detectChanges();

    const imagens = fixture.nativeElement.querySelectorAll(
      '.album-backdrop__capa'
    );

    expect(imagens.length).toBe(0);
  });

  it('deve trocar para a capa padrão quando a imagem falhar', () => {
    fixture.detectChanges();

    httpMock.expectOne(apiUrl).flush([
      { id: 1, titulo: 'Album 1', capaUrl: '/capa-quebrada.jpg' }
    ]);
    fixture.detectChanges();

    const imagem = fixture.nativeElement.querySelector(
      '.album-backdrop__capa'
    ) as HTMLImageElement;

    imagem.dispatchEvent(new Event('error'));

    expect(imagem.src).toContain('/capa-padrao.png');
  });
});
