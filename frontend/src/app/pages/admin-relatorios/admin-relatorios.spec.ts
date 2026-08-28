import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { RelatorioCatalogo } from '../../models/RelatorioCatalogo';
import { AdminRelatorioService } from '../../services/admin-relatorio';
import { AdminRelatorios } from './admin-relatorios';

describe('AdminRelatorios', () => {
  let component: AdminRelatorios;
  let fixture: ComponentFixture<AdminRelatorios>;

  const gerarCatalogo = vi.fn();
  const exportarCatalogo = vi.fn();
  const criarUrl = vi.fn(() => 'blob:relatorio');
  const revogarUrl = vi.fn();
  const relatorio: RelatorioCatalogo = {
    geradoEm: '2026-08-28T12:00:00Z',
    resumo: {
      totalArtistas: 2,
      totalAlbuns: 2,
      totalMusicas: 4,
      totalParticipacoes: 1,
      duracaoTotalSegundos: 7620,
    },
    artistas: [
      {
        idArtista: 1,
        nome: 'Beyoncé',
        totalAlbuns: 1,
        totalMusicasPrincipais: 2,
        totalParticipacoes: 1,
        duracaoTotalSegundos: 3900,
      },
      {
        idArtista: 2,
        nome: 'Queen',
        totalAlbuns: 1,
        totalMusicasPrincipais: 2,
        totalParticipacoes: 0,
        duracaoTotalSegundos: 3720,
      },
    ],
    albuns: [
      {
        idAlbum: 1,
        titulo: 'Águas de Março',
        nomeArtista: 'Elis Regina',
        anoLancamento: 1974,
        totalMusicas: 2,
        duracaoTotalSegundos: 3900,
      },
      {
        idAlbum: 2,
        titulo: 'A Night at the Opera',
        nomeArtista: 'Queen',
        anoLancamento: 1975,
        totalMusicas: 2,
        duracaoTotalSegundos: 3720,
      },
    ],
  };

  beforeEach(async () => {
    gerarCatalogo.mockReset();
    exportarCatalogo.mockReset();
    criarUrl.mockClear();
    revogarUrl.mockClear();

    Object.defineProperty(window.URL, 'createObjectURL', {
      configurable: true,
      value: criarUrl,
    });
    Object.defineProperty(window.URL, 'revokeObjectURL', {
      configurable: true,
      value: revogarUrl,
    });

    await TestBed.configureTestingModule({
      imports: [AdminRelatorios],
      providers: [
        provideRouter([]),
        {
          provide: AdminRelatorioService,
          useValue: { gerarCatalogo, exportarCatalogo },
        },
      ],
    }).compileComponents();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  function criarComponente(): void {
    fixture = TestBed.createComponent(AdminRelatorios);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('deve exibir o resumo e o relatório de artistas', () => {
    gerarCatalogo.mockReturnValue(of(relatorio));

    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;

    expect(gerarCatalogo).toHaveBeenCalledOnce();
    expect(elemento.querySelectorAll('.resumo-grid > div')).toHaveLength(5);
    expect(elemento.textContent).toContain('Beyoncé');
    expect(elemento.textContent).toContain('Queen');
    expect(elemento.textContent).toContain('2h 7min');
  });

  it('deve alternar para álbuns e filtrar sem diferenciar acentos', () => {
    gerarCatalogo.mockReturnValue(of(relatorio));
    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    elemento.querySelector<HTMLButtonElement>('#aba-albuns')?.click();
    fixture.detectChanges();

    const busca = elemento.querySelector<HTMLInputElement>('#busca-relatorio');
    if (busca) {
      busca.value = 'aguas';
      busca.dispatchEvent(new Event('input'));
    }
    fixture.detectChanges();

    expect(component.abaAtiva()).toBe('ALBUNS');
    expect(component.albunsFiltrados()).toHaveLength(1);
    expect(elemento.textContent).toContain('Águas de Março');
    expect(elemento.textContent).not.toContain('A Night at the Opera');

    if (busca) {
      busca.value = 'álbum inexistente';
      busca.dispatchEvent(new Event('input'));
    }
    fixture.detectChanges();

    expect(elemento.textContent).toContain('Nenhum álbum encontrado.');

    elemento.querySelector<HTMLButtonElement>('#aba-artistas')?.click();
    fixture.detectChanges();

    expect(component.abaAtiva()).toBe('ARTISTAS');
  });

  it('deve filtrar artistas e exibir o estado vazio', () => {
    gerarCatalogo.mockReturnValue(of(relatorio));
    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    const busca = elemento.querySelector<HTMLInputElement>('#busca-relatorio');
    if (busca) {
      busca.value = 'artista inexistente';
      busca.dispatchEvent(new Event('input'));
    }
    fixture.detectChanges();

    expect(component.artistasFiltrados()).toEqual([]);
    expect(elemento.textContent).toContain('Nenhum artista encontrado.');
  });

  it('deve bloquear carregamentos duplicados enquanto aguarda a API', () => {
    const resposta = new Subject<RelatorioCatalogo>();
    gerarCatalogo.mockReturnValue(resposta.asObservable());

    criarComponente();
    component.carregarRelatorio();

    expect(component.carregando()).toBe(true);
    expect(gerarCatalogo).toHaveBeenCalledOnce();

    resposta.next(relatorio);
    resposta.complete();
    fixture.detectChanges();

    expect(component.carregando()).toBe(false);
    expect(component.relatorio()).toEqual(relatorio);
  });

  it('deve informar o erro e permitir gerar novamente', () => {
    gerarCatalogo
      .mockReturnValueOnce(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 500,
            }),
        ),
      )
      .mockReturnValueOnce(of(relatorio));

    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelector('[role="alert"]')?.textContent).toContain(
      'Não foi possível gerar os relatórios.',
    );

    elemento.querySelector<HTMLButtonElement>('.error-card .primary-button')?.click();
    fixture.detectChanges();

    expect(gerarCatalogo).toHaveBeenCalledTimes(2);
    expect(component.relatorio()).toEqual(relatorio);
  });

  it.each([
    [0, 'Não foi possível conectar ao servidor.'],
    [401, 'Sua sessão expirou. Faça login novamente.'],
    [403, 'Você não possui permissão para visualizar relatórios.'],
  ])('deve tratar o erro HTTP %i ao gerar', (status, mensagem) => {
    gerarCatalogo.mockReturnValue(throwError(() => new HttpErrorResponse({ status })));

    criarComponente();

    expect(component.mensagemErro()).toBe(mensagem);
    expect(component.relatorio()).toBeNull();
  });

  it('deve exportar o relatório da aba ativa em CSV', () => {
    const arquivo = new Blob(['Artista;Álbuns'], {
      type: 'text/csv',
    });
    gerarCatalogo.mockReturnValue(of(relatorio));
    exportarCatalogo.mockReturnValue(of(arquivo));
    const clicar = vi
      .spyOn(HTMLAnchorElement.prototype, 'click')
      .mockImplementation(() => undefined);
    criarComponente();

    component.exportarCsv('ARTISTAS');

    expect(exportarCatalogo).toHaveBeenCalledWith('ARTISTAS');
    expect(criarUrl).toHaveBeenCalledWith(arquivo);
    expect(clicar).toHaveBeenCalledOnce();
    expect(revogarUrl).toHaveBeenCalledWith('blob:relatorio');
    expect(component.exportando()).toBeNull();
  });

  it('deve bloquear exportações simultâneas', () => {
    const resposta = new Subject<Blob>();
    gerarCatalogo.mockReturnValue(of(relatorio));
    exportarCatalogo.mockReturnValue(resposta.asObservable());
    criarComponente();

    const elemento = fixture.nativeElement as HTMLElement;
    elemento.querySelector<HTMLButtonElement>('.ferramentas .primary-button')?.click();
    fixture.detectChanges();
    component.exportarCsv('ALBUNS');

    expect(exportarCatalogo).toHaveBeenCalledOnce();
    expect(component.exportando()).toBe('ARTISTAS');
    expect(elemento.textContent).toContain('Exportando...');

    resposta.complete();
    expect(component.exportando()).toBeNull();
  });

  it.each([
    [403, 'Você não possui permissão para exportar relatórios.'],
    [500, 'Não foi possível exportar o relatório em CSV.'],
  ])('deve tratar o erro HTTP %i na exportação', (status, mensagem) => {
    gerarCatalogo.mockReturnValue(of(relatorio));
    exportarCatalogo.mockReturnValue(throwError(() => new HttpErrorResponse({ status })));
    criarComponente();

    component.exportarCsv('ALBUNS');
    fixture.detectChanges();

    expect(component.mensagemErroExportacao()).toBe(mensagem);
    expect(component.exportando()).toBeNull();
    expect(
      (fixture.nativeElement as HTMLElement).querySelector('.export-error')?.textContent,
    ).toContain(mensagem);
  });

  it('deve informar quando o navegador não suporta o download', () => {
    gerarCatalogo.mockReturnValue(of(relatorio));
    exportarCatalogo.mockReturnValue(of(new Blob(['dados'])));
    Object.defineProperty(window.URL, 'createObjectURL', {
      configurable: true,
      value: undefined,
    });
    criarComponente();

    component.exportarCsv('ALBUNS');

    expect(component.mensagemErroExportacao()).toBe(
      'O download não está disponível neste navegador.',
    );
  });

  it('deve formatar durações menores e maiores que uma hora', () => {
    gerarCatalogo.mockReturnValue(of(relatorio));
    criarComponente();

    expect(component.formatarDuracao(3599)).toBe('59min');
    expect(component.formatarDuracao(3660)).toBe('1h 1min');
  });
});
