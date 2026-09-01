import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { CurtidaService } from '../../services/curtida';
import { CurtirBotao } from './curtir-botao';

describe('CurtirBotao', () => {
  let component: CurtirBotao;
  let fixture: ComponentFixture<CurtirBotao>;
  let curtidaServiceMock: {
    curtirMusica: ReturnType<typeof vi.fn>;
    descurtirMusica: ReturnType<typeof vi.fn>;
    curtirAlbum: ReturnType<typeof vi.fn>;
    descurtirAlbum: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    curtidaServiceMock = {
      curtirMusica: vi.fn().mockReturnValue(of(undefined)),
      descurtirMusica: vi.fn().mockReturnValue(of(undefined)),
      curtirAlbum: vi.fn().mockReturnValue(of(undefined)),
      descurtirAlbum: vi.fn().mockReturnValue(of(undefined))
    };

    await TestBed.configureTestingModule({
      imports: [CurtirBotao],
      providers: [
        { provide: CurtidaService, useValue: curtidaServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CurtirBotao);
    component = fixture.componentInstance;
  });

  afterEach(() => vi.restoreAllMocks());

  function configurar(
    tipo: 'musica' | 'album',
    id: number,
    curtidoInicial: boolean
  ): void {
    fixture.componentRef.setInput('tipo', tipo);
    fixture.componentRef.setInput('id', id);
    fixture.componentRef.setInput('curtidoInicial', curtidoInicial);
    fixture.detectChanges();
  }

  function botao(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button');
  }

  function criarEvento(): Event {
    return {
      preventDefault: vi.fn(),
      stopPropagation: vi.fn()
    } as unknown as Event;
  }

  it('deve iniciar refletindo o valor de curtidoInicial', () => {
    configurar('musica', 10, true);

    expect(botao().getAttribute('aria-pressed')).toBe('true');
    expect(botao().classList.contains('curtir-botao--ativo')).toBe(true);
  });

  it('deve curtir uma música ao alternar a partir de não curtido', () => {
    configurar('musica', 10, false);

    component.alternar(criarEvento());

    expect(curtidaServiceMock.curtirMusica).toHaveBeenCalledWith(10);
    expect(curtidaServiceMock.descurtirMusica).not.toHaveBeenCalled();
  });

  it('deve descurtir uma música ao alternar a partir de curtido', () => {
    configurar('musica', 10, true);

    component.alternar(criarEvento());

    expect(curtidaServiceMock.descurtirMusica).toHaveBeenCalledWith(10);
  });

  it('deve curtir um álbum ao alternar a partir de não curtido', () => {
    configurar('album', 20, false);

    component.alternar(criarEvento());

    expect(curtidaServiceMock.curtirAlbum).toHaveBeenCalledWith(20);
  });

  it('deve descurtir um álbum ao alternar a partir de curtido', () => {
    configurar('album', 20, true);

    component.alternar(criarEvento());

    expect(curtidaServiceMock.descurtirAlbum).toHaveBeenCalledWith(20);
  });

  it('deve emitir curtidoChange com o novo valor ao concluir com sucesso', () => {
    configurar('musica', 10, false);
    const emitido: boolean[] = [];
    component.curtidoChange.subscribe(valor => emitido.push(valor));

    component.alternar(criarEvento());
    fixture.detectChanges();

    expect(emitido).toEqual([true]);
    expect(botao().getAttribute('aria-pressed')).toBe('true');
  });

  it('deve reverter o estado quando a chamada falhar', () => {
    curtidaServiceMock.curtirMusica.mockReturnValue(
      throwError(() => new Error('falhou'))
    );
    configurar('musica', 10, false);
    const emitido: boolean[] = [];
    component.curtidoChange.subscribe(valor => emitido.push(valor));

    component.alternar(criarEvento());
    fixture.detectChanges();

    expect(emitido).toEqual([]);
    expect(botao().getAttribute('aria-pressed')).toBe('false');
    expect(botao().disabled).toBe(false);
  });

  it('deve ignorar cliques repetidos enquanto uma chamada está em andamento', () => {
    const pendente = new Subject<void>();
    curtidaServiceMock.curtirMusica.mockReturnValue(pendente.asObservable());
    configurar('musica', 10, false);

    component.alternar(criarEvento());
    component.alternar(criarEvento());
    fixture.detectChanges();

    expect(botao().disabled).toBe(true);
    expect(curtidaServiceMock.curtirMusica).toHaveBeenCalledOnce();

    pendente.next();
    pendente.complete();
    fixture.detectChanges();

    expect(botao().disabled).toBe(false);
  });

  it('deve impedir a propagação do clique e a navegação padrão', () => {
    configurar('musica', 10, false);
    const evento = criarEvento();

    component.alternar(evento);

    expect(evento.preventDefault).toHaveBeenCalled();
    expect(evento.stopPropagation).toHaveBeenCalled();
  });

  it('deve alterar o rótulo acessível conforme o estado de curtida', () => {
    configurar('musica', 10, false);
    expect(botao().getAttribute('aria-label')).toBe('Curtir');

    component.alternar(criarEvento());
    fixture.detectChanges();

    expect(botao().getAttribute('aria-label')).toBe('Remover dos curtidos');
  });
});
