import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { ArtistaResponse } from '../../models/ArtistaResponse';
import { AdminArtistaService } from '../../services/admin-artista';
import { AdminArtistas } from './admin-artistas';

describe('AdminArtistas', () => {
  let component: AdminArtistas;
  let fixture: ComponentFixture<AdminArtistas>;
  let router: Router;

  const listarArtistas = vi.fn();
  const excluir = vi.fn();
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
    excluir.mockReset();

    await TestBed.configureTestingModule({
      imports: [AdminArtistas],
      providers: [
        provideRouter([]),
        {
          provide: AdminArtistaService,
          useValue: { listarArtistas, excluir }
        }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => {
    vi.restoreAllMocks();
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
    const botoesEdicao = elemento.querySelectorAll<HTMLButtonElement>(
      'button[aria-label^="Editar"]'
    );
    const botoesExclusao = elemento.querySelectorAll<HTMLButtonElement>(
      'button[aria-label^="Excluir"]'
    );

    expect(listarArtistas).toHaveBeenCalledOnce();
    expect(cards).toHaveLength(2);
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
    expect(botoesEdicao).toHaveLength(2);
    expect(botoesExclusao).toHaveLength(2);
    expect(botoesEdicao[0].getAttribute('aria-label'))
      .toBe('Editar Queen');
    expect(botoesExclusao[0].getAttribute('aria-label'))
      .toBe('Excluir Queen');
  });

  it('deve exibir o estado de carregamento', () => {
    const resposta = new Subject<ArtistaResponse[]>();
    listarArtistas.mockReturnValue(resposta.asObservable());

    criarComponente();

    expect(component.carregando()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Carregando artistas...');

    component.carregarArtistas();
    expect(listarArtistas).toHaveBeenCalledOnce();

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
    expect(elemento.querySelectorAll('.artista-card')).toHaveLength(2);
  });

  it.each([
    [0, 'Não foi possível conectar ao servidor.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não possui permissão para visualizar os artistas.'],
    [418, 'Não foi possível carregar os artistas.']
  ])('deve tratar o erro HTTP %i ao carregar', (status, mensagem) => {
    listarArtistas.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status })
    ));

    criarComponente();

    expect(component.mensagemErro()).toBe(mensagem);
    expect(component.artistas()).toEqual([]);
  });

  it('deve substituir uma foto que falhar pela imagem alternativa', () => {
    listarArtistas.mockReturnValue(of([artistas[0]]));
    criarComponente();

    const imagem = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLImageElement>('.artista-foto');

    imagem?.dispatchEvent(new Event('error'));
    imagem?.dispatchEvent(new Event('error'));

    expect(imagem?.src).toMatch(/\/avatar-artista\.png$/);
  });

  it('deve navegar para a edição do artista', () => {
    listarArtistas.mockReturnValue(of(artistas));
    criarComponente();

    const botaoEditar = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>(
      'button[aria-label="Editar Queen"]'
    );
    botaoEditar?.click();

    expect(router.navigate).toHaveBeenCalledWith([
      '/admin/banco/artistas',
      1,
      'editar'
    ]);
  });

  it('não deve excluir quando a confirmação for cancelada', () => {
    listarArtistas.mockReturnValue(of(artistas));
    const confirmar = vi
      .spyOn(window, 'confirm')
      .mockReturnValue(false);
    criarComponente();

    const botaoExcluir = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>(
      'button[aria-label="Excluir Queen"]'
    );
    botaoExcluir?.click();

    expect(confirmar).toHaveBeenCalledWith(
      expect.stringContaining('Queen')
    );
    expect(excluir).not.toHaveBeenCalled();
  });

  it('deve excluir após a confirmação e atualizar a listagem', () => {
    listarArtistas
      .mockReturnValueOnce(of(artistas))
      .mockReturnValueOnce(of([artistas[1]]));
    excluir.mockReturnValue(of(undefined));
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    const botaoExcluir = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>(
      'button[aria-label="Excluir Queen"]'
    );
    botaoExcluir?.click();
    fixture.detectChanges();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(excluir).toHaveBeenCalledWith(1);
    expect(listarArtistas).toHaveBeenCalledTimes(2);
    expect(component.artistas()).toEqual([artistas[1]]);
    expect(elemento.querySelectorAll('.artista-card')).toHaveLength(1);
    expect(elemento.querySelector('.success-feedback')?.textContent)
      .toContain('Artista Queen excluído com sucesso!');
  });

  it('deve bloquear as ações durante a exclusão', () => {
    const resposta = new Subject<void>();
    listarArtistas.mockReturnValue(of(artistas));
    excluir.mockReturnValue(resposta.asObservable());
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    elemento.querySelector<HTMLButtonElement>(
      'button[aria-label="Excluir Queen"]'
    )?.click();
    fixture.detectChanges();

    const botoes = elemento.querySelectorAll<HTMLButtonElement>(
      '.card-button'
    );
    expect(Array.from(botoes).every(botao => botao.disabled))
      .toBe(true);
    expect(botoes[1].textContent).toContain('Excluindo...');
    expect(botoes[1].getAttribute('aria-busy')).toBe('true');

    component.editarArtista(2);
    component.excluirArtista(artistas[1]);

    expect(router.navigate).not.toHaveBeenCalled();
    expect(excluir).toHaveBeenCalledOnce();

    resposta.complete();
    fixture.detectChanges();

    expect(component.operacaoEmAndamento()).toBe(false);
  });

  it('deve manter o artista e informar as dependências no conflito', () => {
    const mensagem =
      'Não é possível excluir o artista porque ele possui álbuns associados.';
    listarArtistas.mockReturnValue(of(artistas));
    excluir.mockReturnValue(throwError(() => new HttpErrorResponse({
      status: 409,
      error: { message: mensagem }
    })));
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    component.excluirArtista(artistas[0]);
    fixture.detectChanges();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(component.artistas()).toEqual(artistas);
    expect(elemento.querySelectorAll('.artista-card')).toHaveLength(2);
    expect(elemento.querySelector('.error-feedback')?.textContent)
      .toContain(mensagem);
    expect(listarArtistas).toHaveBeenCalledOnce();
  });

  it('deve usar mensagem de dependências quando a API não detalhar o conflito', () => {
    listarArtistas.mockReturnValue(of(artistas));
    excluir.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 409 })
    ));
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    component.excluirArtista(artistas[0]);

    expect(component.mensagemErroExclusao()).toContain(
      'possui músicas ou álbuns associados'
    );
  });

  it.each([
    [0, 'Não foi possível conectar ao servidor.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não possui permissão para excluir artistas.'],
    [404, 'O artista não foi encontrado. Atualize a listagem.'],
    [500, 'Ocorreu um erro no servidor ao excluir o artista.'],
    [418, 'Não foi possível excluir o artista.']
  ])('deve tratar o erro HTTP %i na exclusão', (status, mensagem) => {
    listarArtistas.mockReturnValue(of(artistas));
    excluir.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status })
    ));
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    component.excluirArtista(artistas[0]);

    expect(component.mensagemErroExclusao()).toBe(mensagem);
    expect(component.artistas()).toEqual(artistas);
  });
});
