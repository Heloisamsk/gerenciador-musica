import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AlbumResponse } from '../../models/AlbumResponse';
import { AdminAlbumService } from '../../services/admin-album.service';
import { AdminAlbuns } from './admin-albuns';

describe('AdminAlbuns', () => {
  let component: AdminAlbuns;
  let fixture: ComponentFixture<AdminAlbuns>;

  const listarAlbuns = vi.fn();
  const albuns: AlbumResponse[] = [
    {
      idAlbum: 1,
      titulo: 'A Night at the Opera',
      anoLancamento: 1975,
      capaUrl: 'https://example.com/capa.jpg',
      artista: {
        id: 1,
        nome: 'Queen',
        nomeCompleto: 'Queen',
        descricao: 'Banda britânica de rock.',
        fotoPerfilUrl: null
      }
    },
    {
      idAlbum: 2,
      titulo: 'Elis & Tom',
      anoLancamento: 1974,
      capaUrl: null,
      artista: {
        id: 2,
        nome: 'Elis Regina',
        nomeCompleto: 'Elis Regina Carvalho Costa',
        descricao: 'Cantora brasileira.',
        fotoPerfilUrl: null
      }
    }
  ];

  beforeEach(async () => {
    listarAlbuns.mockReset();

    await TestBed.configureTestingModule({
      imports: [AdminAlbuns],
      providers: [
        provideRouter([]),
        {
          provide: AdminAlbumService,
          useValue: { listarAlbuns }
        }
      ]
    }).compileComponents();
  });

  function criarComponente(): void {
    fixture = TestBed.createComponent(AdminAlbuns);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('deve listar os álbuns, artistas, anos e quantidade total', () => {
    listarAlbuns.mockReturnValue(of(albuns));

    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    const cards = elemento.querySelectorAll('.album-card');
    const capas = elemento.querySelectorAll<HTMLImageElement>(
      '.album-capa'
    );

    expect(listarAlbuns).toHaveBeenCalledOnce();
    expect(cards).toHaveLength(2);
    expect(elemento.textContent).toContain('Total de álbuns:');
    expect(elemento.querySelector('.total-albuns strong')?.textContent)
      .toContain('2');
    expect(elemento.textContent).toContain('A Night at the Opera');
    expect(elemento.textContent).toContain('Queen');
    expect(elemento.textContent).toContain('1975');
    expect(elemento.textContent).toContain('Elis & Tom');
    expect(elemento.textContent).toContain('Elis Regina');
    expect(capas[0].getAttribute('src'))
      .toBe('https://example.com/capa.jpg');
    expect(capas[1].getAttribute('src'))
      .toBe('/capa-padrao.png');
    expect(capas[0].getAttribute('alt'))
      .toBe('Capa do álbum A Night at the Opera');
  });

  it('deve exibir carregamento e impedir chamadas duplicadas', () => {
    const resposta = new Subject<AlbumResponse[]>();
    listarAlbuns.mockReturnValue(resposta.asObservable());

    criarComponente();

    expect(component.carregando()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Carregando álbuns...');

    component.carregarAlbuns();
    expect(listarAlbuns).toHaveBeenCalledOnce();

    resposta.next(albuns);
    resposta.complete();
    fixture.detectChanges();

    expect(component.carregando()).toBe(false);
    expect(component.albuns()).toEqual(albuns);
  });

  it('deve exibir o estado de lista vazia', () => {
    listarAlbuns.mockReturnValue(of([]));

    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.textContent).toContain('Nenhum álbum encontrado');
    expect(elemento.querySelector('.total-albuns strong')?.textContent)
      .toContain('0');
  });

  it('deve exibir erro e permitir tentar novamente', () => {
    listarAlbuns
      .mockReturnValueOnce(throwError(() =>
        new HttpErrorResponse({ status: 500 })
      ))
      .mockReturnValueOnce(of(albuns));

    criarComponente();

    let elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('Ocorreu um erro no servidor');

    elemento.querySelector<HTMLButtonElement>(
      '.error-card .primary-button'
    )?.click();
    fixture.detectChanges();
    elemento = fixture.nativeElement as HTMLElement;

    expect(listarAlbuns).toHaveBeenCalledTimes(2);
    expect(elemento.querySelectorAll('.album-card')).toHaveLength(2);
  });

  it.each([
    [0, 'Não foi possível conectar ao servidor.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não possui permissão para visualizar os álbuns.'],
    [418, 'Não foi possível carregar os álbuns.']
  ])('deve tratar o erro HTTP %i ao carregar', (status, mensagem) => {
    listarAlbuns.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status })
    ));

    criarComponente();

    expect(component.mensagemErro()).toBe(mensagem);
    expect(component.albuns()).toEqual([]);
  });

  it('deve substituir uma capa inválida pela imagem alternativa', () => {
    listarAlbuns.mockReturnValue(of([albuns[0]]));
    criarComponente();

    const imagem = (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLImageElement>('.album-capa');

    imagem?.dispatchEvent(new Event('error'));
    imagem?.dispatchEvent(new Event('error'));

    expect(imagem?.src).toMatch(/\/capa-padrao\.png$/);
  });

  it('deve oferecer navegação para painel e cadastro', () => {
    listarAlbuns.mockReturnValue(of([]));
    criarComponente();

    const links = Array.from(
      (fixture.nativeElement as HTMLElement)
        .querySelectorAll<HTMLAnchorElement>('a')
    ).map(link => link.getAttribute('href'));

    expect(links).toContain('/admin/painel');
    expect(links).toContain('/admin/banco/albuns/novo');
  });
});
