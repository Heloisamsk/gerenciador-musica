import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import type { MusicaListagem } from '../../models/MusicaListagem';
import type { PaginaResponse } from '../../models/PaginaResponse';
import { AdminMusicaService } from '../../services/admin-musica';
import { AdminMusicas } from './admin-musicas';

describe('AdminMusicas', () => {
  let component: AdminMusicas;
  let fixture: ComponentFixture<AdminMusicas>;

  const listarMusicas = vi.fn();
  const excluirMusica = vi.fn();
  const navigate = vi.fn();
  const currentNavigation = vi.fn();

  const musicas: MusicaListagem[] = [
    {
      id: 31,
      titulo: 'Música de Teste Um',
      duracaoSegundos: 210,
      anoLancamento: 2024,
      artistaPrincipal: {
        id: 7,
        nome: 'Artista Principal'
      },
      album: {
        id: 11,
        titulo: 'Álbum de Teste',
        anoLancamento: 2024,
        capaUrl: 'https://example.com/capa-teste.jpg'
      },
      artistasParticipantes: [
        { id: 9, nome: 'Participante Um' },
        { id: 10, nome: 'Participante Dois' }
      ],
      generos: [
        { id: 1, nome: 'Gênero Um' },
        { id: 2, nome: 'Gênero Dois' }
      ]
    },
    {
      id: 32,
      titulo: 'Música de Teste Dois',
      duracaoSegundos: 180,
      anoLancamento: 2025,
      artistaPrincipal: {
        id: 8,
        nome: 'Outro Artista'
      },
      album: null,
      artistasParticipantes: [],
      generos: []
    }
  ];

  function criarPagina(
    itens: MusicaListagem[] = musicas,
    paginaAtual = 0,
    totalItens = itens.length,
    totalPaginas = totalItens === 0 ? 0 : 1
  ): PaginaResponse<MusicaListagem> {
    return {
      itens,
      paginaAtual,
      tamanhoPagina: 20,
      totalItens,
      totalPaginas
    };
  }

  beforeEach(async () => {
    listarMusicas.mockReset();
    excluirMusica.mockReset();
    navigate.mockReset();
    currentNavigation.mockReset();

    listarMusicas.mockReturnValue(of(criarPagina()));
    excluirMusica.mockReturnValue(of(undefined));
    navigate.mockResolvedValue(true);
    currentNavigation.mockReturnValue(null);

    await TestBed.configureTestingModule({
      imports: [AdminMusicas],
      providers: [
        {
          provide: AdminMusicaService,
          useValue: {
            listarMusicas,
            excluirMusica
          }
        },
        {
          provide: Router,
          useValue: {
            navigate,
            currentNavigation
          }
        }
      ]
    }).compileComponents();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  function criarComponente(): void {
    fixture = TestBed.createComponent(AdminMusicas);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('deve listar músicas, associações, total e ações acessíveis', () => {
    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    const linhas = elemento.querySelectorAll('tbody tr');
    const botoesEditar = elemento.querySelectorAll<HTMLButtonElement>(
      'button[aria-label^="Editar música"]'
    );
    const botoesExcluir = elemento.querySelectorAll<HTMLButtonElement>(
      'button[aria-label^="Excluir música"]'
    );

    expect(listarMusicas).toHaveBeenCalledWith(0, 20);
    expect(linhas).toHaveLength(2);
    expect(elemento.querySelectorAll('thead th')).toHaveLength(9);
    expect(elemento.textContent).toContain('Total de músicas:');
    expect(elemento.querySelector('.table-information strong')?.textContent)
      .toContain('2');
    expect(elemento.textContent).toContain('Música de Teste Um');
    expect(elemento.textContent).toContain('Artista Principal');
    expect(elemento.textContent).toContain('Álbum de Teste');
    expect(elemento.textContent).toContain('Participante Um');
    expect(elemento.textContent).toContain('Participante Dois');
    expect(elemento.textContent).toContain('Gênero Um, Gênero Dois');
    expect(elemento.textContent).toContain('Música de Teste Dois');
    expect(botoesEditar).toHaveLength(2);
    expect(botoesExcluir).toHaveLength(2);
    expect(botoesEditar[0].getAttribute('aria-label'))
      .toBe('Editar música Música de Teste Um');
    expect(botoesExcluir[0].getAttribute('aria-label'))
      .toBe('Excluir música Música de Teste Um');
    expect(elemento.querySelector<HTMLImageElement>('.capa-album')?.src)
      .toBe('https://example.com/capa-teste.jpg');
    expect(elemento.querySelector('.capa-alternativa .texto-acessivel')
      ?.textContent).toContain('Música sem álbum');
  });

  it('deve navegar por todas as páginas e manter o total real', () => {
    listarMusicas
      .mockReturnValueOnce(of(criarPagina(musicas, 0, 45, 3)))
      .mockReturnValueOnce(of(criarPagina([musicas[1]], 1, 45, 3)))
      .mockReturnValueOnce(of(criarPagina(musicas, 0, 45, 3)))
      .mockReturnValueOnce(of(criarPagina([musicas[0]], 2, 45, 3)))
      .mockReturnValueOnce(of(criarPagina(musicas, 0, 45, 3)));
    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelector('.table-information')?.textContent)
      .toContain('45');
    expect(elemento.querySelector('.paginacao-status')?.textContent)
      .toContain('Página 1 de 3');

    const botoesPaginacao = Array.from(
      elemento.querySelectorAll<HTMLButtonElement>('.paginacao-button')
    );

    for (const botao of botoesPaginacao) {
      expect(botao.getAttribute('aria-label'))
        .toBe(botao.textContent?.trim());
    }

    elemento.querySelector<HTMLButtonElement>(
      'button[aria-label="Próxima"]'
    )?.click();
    fixture.detectChanges();

    expect(listarMusicas).toHaveBeenNthCalledWith(2, 1, 20);
    expect(component.paginaAtual()).toBe(1);
    expect(component.primeiroItemExibido()).toBe(21);
    expect(component.ultimoItemExibido()).toBe(40);

    elemento.querySelector<HTMLButtonElement>(
      'button[aria-label="Anterior"]'
    )?.click();
    fixture.detectChanges();
    elemento.querySelector<HTMLButtonElement>(
      'button[aria-label="Última"]'
    )?.click();
    fixture.detectChanges();
    elemento.querySelector<HTMLButtonElement>(
      'button[aria-label="Primeira"]'
    )?.click();
    fixture.detectChanges();

    expect(listarMusicas).toHaveBeenNthCalledWith(3, 0, 20);
    expect(listarMusicas).toHaveBeenNthCalledWith(4, 2, 20);
    expect(listarMusicas).toHaveBeenNthCalledWith(5, 0, 20);
    expect(component.paginaAtual()).toBe(0);

    component.irParaPagina(-1);
    component.irParaPagina(3);
    component.irParaPagina(0);
    expect(listarMusicas).toHaveBeenCalledTimes(5);
  });

  it('deve substituir uma capa inválida pela imagem alternativa', () => {
    criarComponente();
    const elemento = fixture.nativeElement as HTMLElement;
    const imagem = elemento.querySelector<HTMLImageElement>('img.capa-album');

    imagem?.dispatchEvent(new Event('error'));
    fixture.detectChanges();

    expect(elemento.querySelectorAll('.capa-alternativa')).toHaveLength(2);
    expect(elemento.querySelector('.capa-alternativa .texto-acessivel')
      ?.textContent).toContain(
        'Capa indisponível para o álbum Álbum de Teste'
      );
  });

  it('deve exibir carregamento e impedir chamadas duplicadas', () => {
    const resposta = new Subject<PaginaResponse<MusicaListagem>>();
    listarMusicas.mockReturnValue(resposta.asObservable());

    criarComponente();

    expect(component.carregando()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Carregando músicas...');

    component.carregarCatalogo();
    expect(listarMusicas).toHaveBeenCalledOnce();

    resposta.next(criarPagina());
    resposta.complete();
    fixture.detectChanges();

    expect(component.carregando()).toBe(false);
    expect(component.musicas()).toEqual(musicas);
  });

  it('deve exibir o estado de lista vazia', () => {
    listarMusicas.mockReturnValue(of(criarPagina([])));

    criarComponente();

    expect((fixture.nativeElement as HTMLElement).textContent)
      .toContain('Nenhuma música encontrada');
  });

  it('deve exibir erro ao carregar e permitir tentar novamente', () => {
    listarMusicas
      .mockReturnValueOnce(throwError(() =>
        new HttpErrorResponse({ status: 500 })
      ))
      .mockReturnValueOnce(of(criarPagina()));

    criarComponente();

    let elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('Ocorreu um erro no servidor');

    elemento.querySelector<HTMLButtonElement>(
      '.tentar-novamente-button'
    )?.click();
    fixture.detectChanges();
    elemento = fixture.nativeElement as HTMLElement;

    expect(listarMusicas).toHaveBeenCalledTimes(2);
    expect(elemento.querySelectorAll('tbody tr')).toHaveLength(2);
  });

  it.each([
    [0, 'Não foi possível conectar ao servidor.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não possui permissão para visualizar as músicas.'],
    [418, 'Não foi possível carregar o catálogo de músicas.']
  ])('deve tratar o erro HTTP %i ao carregar', (status, mensagem) => {
    listarMusicas.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status })
    ));

    criarComponente();

    expect(component.mensagemErro()).toBe(mensagem);
    expect(component.musicas()).toEqual([]);
  });

  it('deve recuperar a mensagem de sucesso da navegação', () => {
    currentNavigation.mockReturnValue({
      extras: {
        state: {
          mensagemSucesso: 'Música atualizada com sucesso!'
        }
      }
    });

    criarComponente();

    expect(component.mensagemSucesso())
      .toBe('Música atualizada com sucesso!');
    expect((fixture.nativeElement as HTMLElement)
      .querySelector('.feedback-sucesso')?.textContent)
      .toContain('Música atualizada com sucesso!');
  });

  it('deve navegar para a edição da música selecionada', () => {
    criarComponente();

    const botaoEditar = (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>(
        'button[aria-label="Editar música Música de Teste Um"]'
      );
    botaoEditar?.click();

    expect(navigate).toHaveBeenCalledWith([
      '/admin/banco/musicas',
      31,
      'editar'
    ]);
  });

  it('não deve chamar a API quando a exclusão for cancelada', () => {
    const confirmar = vi.spyOn(window, 'confirm')
      .mockReturnValue(false);
    criarComponente();

    const botaoExcluir = (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>(
        'button[aria-label="Excluir música Música de Teste Um"]'
      );
    botaoExcluir?.click();

    expect(confirmar).toHaveBeenCalledWith(
      'Tem certeza que deseja excluir a música "Música de Teste Um"?'
    );
    expect(excluirMusica).not.toHaveBeenCalled();
  });

  it('deve excluir após confirmação e atualizar a listagem', () => {
    listarMusicas
      .mockReturnValueOnce(of(criarPagina()))
      .mockReturnValueOnce(of(criarPagina([musicas[1]])));
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    const botaoExcluir = (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>(
        'button[aria-label="Excluir música Música de Teste Um"]'
      );
    botaoExcluir?.click();
    fixture.detectChanges();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(excluirMusica).toHaveBeenCalledOnce();
    expect(excluirMusica).toHaveBeenCalledWith(31);
    expect(listarMusicas).toHaveBeenCalledTimes(2);
    expect(component.musicas()).toEqual([musicas[1]]);
    expect(elemento.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(elemento.querySelector('.feedback-sucesso')?.textContent)
      .toContain('Música Música de Teste Um excluída com sucesso!');
  });

  it('deve voltar uma página ao excluir o único item da página atual', () => {
    listarMusicas
      .mockReturnValueOnce(of(criarPagina([musicas[0]], 1, 21, 2)))
      .mockReturnValueOnce(of(criarPagina(musicas, 0, 20, 1)));
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    component.excluirMusica(musicas[0]);

    expect(listarMusicas).toHaveBeenNthCalledWith(2, 0, 20);
    expect(component.paginaAtual()).toBe(0);
  });

  it('deve bloquear as ações durante a exclusão', () => {
    const resposta = new Subject<void>();
    excluirMusica.mockReturnValue(resposta.asObservable());
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    const botaoExcluir = elemento.querySelector<HTMLButtonElement>(
      'button[aria-label="Excluir música Música de Teste Um"]'
    );
    botaoExcluir?.click();
    fixture.detectChanges();

    const botoes = Array.from(
      elemento.querySelectorAll<HTMLButtonElement>('.acao-button')
    );
    expect(botoes.every(botao => botao.disabled)).toBe(true);
    expect(botaoExcluir?.getAttribute('aria-busy')).toBe('true');
    expect(botaoExcluir?.textContent).toContain('Excluindo...');

    component.editarMusica(32);
    component.excluirMusica(musicas[1]);

    expect(navigate).not.toHaveBeenCalled();
    expect(excluirMusica).toHaveBeenCalledOnce();

    resposta.complete();
    fixture.detectChanges();

    expect(component.operacaoEmAndamento()).toBe(false);
  });

  it('deve manter a lista e exibir erro quando a exclusão falhar', () => {
    excluirMusica.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 500 })
    ));
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    component.excluirMusica(musicas[0]);
    fixture.detectChanges();

    expect(component.musicas()).toEqual(musicas);
    expect(listarMusicas).toHaveBeenCalledOnce();
    expect((fixture.nativeElement as HTMLElement)
      .querySelector('.feedback-erro')?.textContent)
      .toContain('Ocorreu um erro no servidor ao excluir a música.');
  });

  it.each([
    [0, 'Não foi possível conectar ao servidor.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não possui permissão para excluir músicas.'],
    [404, 'A música não foi encontrada. Atualize a listagem.'],
    [409, 'Não foi possível excluir a música porque ela ainda possui vínculos.'],
    [500, 'Ocorreu um erro no servidor ao excluir a música.'],
    [418, 'Não foi possível excluir a música.']
  ])('deve tratar o erro HTTP %i ao excluir', (status, mensagem) => {
    excluirMusica.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status })
    ));
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    component.excluirMusica(musicas[0]);

    expect(component.mensagemErroExclusao()).toBe(mensagem);
    expect(component.musicas()).toEqual(musicas);
  });

  it('deve priorizar a mensagem da API em um conflito', () => {
    excluirMusica.mockReturnValue(throwError(() =>
      new HttpErrorResponse({
        status: 409,
        error: { message: 'Conflito informado pela API.' }
      })
    ));
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    criarComponente();

    component.excluirMusica(musicas[0]);

    expect(component.mensagemErroExclusao())
      .toBe('Conflito informado pela API.');
  });

  it('deve formatar gêneros e representar ausência com hífen', () => {
    criarComponente();

    expect(component.generosTexto(musicas[0]))
      .toBe('Gênero Um, Gênero Dois');
    expect(component.generosTexto(musicas[1])).toBe('-');
  });
});
