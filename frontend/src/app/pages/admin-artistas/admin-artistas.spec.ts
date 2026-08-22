import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { ArtistaResponse } from '../../models/ArtistaResponse';
import { AdminArtistaService } from '../../services/admin-artista';
import { AdminArtistas } from './admin-artistas';

describe('AdminArtistas', () => {
  let component: AdminArtistas;
  let fixture: ComponentFixture<AdminArtistas>;

  const listarArtistas = vi.fn();
  const artistas: ArtistaResponse[] = [
    {
      idArtista: 1,
      nome: 'Queen',
      nomeCompleto: 'Queen',
      descricao: 'Banda britânica de rock.',
      fotoPerfilUrl: 'https://exemplo.com/queen.jpg'
    },
    {
      idArtista: 2,
      nome: 'Elis Regina',
      nomeCompleto: 'Elis Regina Carvalho Costa',
      descricao: 'Cantora brasileira.',
      fotoPerfilUrl: null
    }
  ];

  beforeEach(async () => {
    listarArtistas.mockReset();

    await TestBed.configureTestingModule({
      imports: [AdminArtistas],
      providers: [
        provideRouter([]),
        {
          provide: AdminArtistaService,
          useValue: { listarArtistas }
        }
      ]
    }).compileComponents();
  });

  function criarComponente(): void {
    fixture = TestBed.createComponent(AdminArtistas);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('deve listar todos os artistas e a quantidade total', () => {
    listarArtistas.mockReturnValue(of(artistas));

    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    const cards = elemento.querySelectorAll('.artista-card');
    const fotos = elemento.querySelectorAll<HTMLImageElement>(
      '.artista-foto'
    );
    const linksEdicao = elemento.querySelectorAll<HTMLAnchorElement>(
      '.edit-link'
    );

    expect(listarArtistas).toHaveBeenCalledOnce();
    expect(cards.length).toBe(2);
    expect(elemento.textContent).toContain('Total de artistas:');
    expect(elemento.textContent).toContain('2');
    expect(elemento.textContent).toContain('Queen');
    expect(elemento.textContent)
      .toContain('Elis Regina Carvalho Costa');
    expect(elemento.textContent).toContain('Cantora brasileira.');
    expect(fotos[0].getAttribute('src'))
      .toBe('https://exemplo.com/queen.jpg');
    expect(fotos[1].getAttribute('src'))
      .toBe('/avatar-artista.png');
    expect(linksEdicao[0].getAttribute('href'))
      .toBe('/admin/banco/artistas/1/editar');
  });

  it('deve exibir o estado de carregamento', () => {
    const resposta = new Subject<ArtistaResponse[]>();
    listarArtistas.mockReturnValue(resposta.asObservable());

    criarComponente();

    expect(component.carregando()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Carregando artistas...');

    resposta.next(artistas);
    resposta.complete();
    fixture.detectChanges();

    expect(component.carregando()).toBe(false);
    expect(component.artistas()).toEqual(artistas);
  });

  it('deve exibir o estado de lista vazia', () => {
    listarArtistas.mockReturnValue(of([]));

    criarComponente();

    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Nenhum artista encontrado');
    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Total de artistas:');
    expect((fixture.nativeElement as HTMLElement)
      .querySelector('.total-artistas strong')?.textContent)
      .toContain('0');
  });

  it('deve exibir erro e permitir tentar novamente', () => {
    listarArtistas
      .mockReturnValueOnce(throwError(() => new HttpErrorResponse({
        status: 500
      })))
      .mockReturnValueOnce(of(artistas));

    criarComponente();

    let elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('Ocorreu um erro no servidor');

    elemento.querySelector<HTMLButtonElement>(
      '.error-card .primary-button'
    )?.click();
    fixture.detectChanges();
    elemento = fixture.nativeElement as HTMLElement;

    expect(listarArtistas).toHaveBeenCalledTimes(2);
    expect(elemento.querySelectorAll('.artista-card').length).toBe(2);
  });

  it('deve substituir uma foto que falhar pela imagem alternativa', () => {
    listarArtistas.mockReturnValue(of([artistas[0]]));
    criarComponente();

    const imagem = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLImageElement>('.artista-foto');

    imagem?.dispatchEvent(new Event('error'));

    expect(imagem?.src).toMatch(/\/avatar-artista\.png$/);
  });
});
