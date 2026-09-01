import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { SeguidorService } from '../../services/seguidor';
import { SeguirBotao } from './seguir-botao';

describe('SeguirBotao', () => {
  let component: SeguirBotao;
  let fixture: ComponentFixture<SeguirBotao>;
  let seguidorServiceMock: {
    seguirUsuario: ReturnType<typeof vi.fn>;
    deixarDeSeguirUsuario: ReturnType<typeof vi.fn>;
    seguirArtista: ReturnType<typeof vi.fn>;
    deixarDeSeguirArtista: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    seguidorServiceMock = {
      seguirUsuario: vi.fn().mockReturnValue(of(undefined)),
      deixarDeSeguirUsuario: vi.fn().mockReturnValue(of(undefined)),
      seguirArtista: vi.fn().mockReturnValue(of(undefined)),
      deixarDeSeguirArtista: vi.fn().mockReturnValue(of(undefined))
    };

    await TestBed.configureTestingModule({
      imports: [SeguirBotao],
      providers: [
        { provide: SeguidorService, useValue: seguidorServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SeguirBotao);
    component = fixture.componentInstance;
  });

  afterEach(() => vi.restoreAllMocks());

  function configurar(
    tipo: 'artista' | 'usuario',
    id: number,
    seguindoInicial: boolean
  ): void {
    fixture.componentRef.setInput('tipo', tipo);
    fixture.componentRef.setInput('id', id);
    fixture.componentRef.setInput('seguindoInicial', seguindoInicial);
    fixture.detectChanges();
  }

  function botao(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button');
  }

  it('deve iniciar refletindo o valor de seguindoInicial', () => {
    configurar('usuario', 2, true);

    expect(botao().getAttribute('aria-pressed')).toBe('true');
    expect(botao().textContent).toContain('Seguindo');
  });

  it('deve usar artista como tipo padrão', () => {
    fixture.componentRef.setInput('id', 5);
    fixture.componentRef.setInput('seguindoInicial', false);
    fixture.detectChanges();

    component.alternar();

    expect(seguidorServiceMock.seguirArtista).toHaveBeenCalledWith(5);
  });

  it('deve seguir um usuário ao alternar a partir de não seguindo', () => {
    configurar('usuario', 2, false);

    component.alternar();

    expect(seguidorServiceMock.seguirUsuario).toHaveBeenCalledWith(2);
    expect(seguidorServiceMock.deixarDeSeguirUsuario).not.toHaveBeenCalled();
  });

  it('deve deixar de seguir um usuário ao alternar a partir de seguindo', () => {
    configurar('usuario', 2, true);

    component.alternar();

    expect(seguidorServiceMock.deixarDeSeguirUsuario).toHaveBeenCalledWith(2);
  });

  it('deve seguir um artista ao alternar a partir de não seguindo', () => {
    configurar('artista', 5, false);

    component.alternar();

    expect(seguidorServiceMock.seguirArtista).toHaveBeenCalledWith(5);
  });

  it('deve deixar de seguir um artista ao alternar a partir de seguindo', () => {
    configurar('artista', 5, true);

    component.alternar();

    expect(seguidorServiceMock.deixarDeSeguirArtista).toHaveBeenCalledWith(5);
  });

  it('deve emitir seguindoChange com o novo valor ao concluir com sucesso', () => {
    configurar('usuario', 2, false);
    const emitido: boolean[] = [];
    component.seguindoChange.subscribe(valor => emitido.push(valor));

    component.alternar();
    fixture.detectChanges();

    expect(emitido).toEqual([true]);
    expect(botao().getAttribute('aria-pressed')).toBe('true');
  });

  it('deve reverter o estado quando a chamada falhar', () => {
    seguidorServiceMock.seguirUsuario.mockReturnValue(
      throwError(() => new Error('falhou'))
    );
    configurar('usuario', 2, false);
    const emitido: boolean[] = [];
    component.seguindoChange.subscribe(valor => emitido.push(valor));

    component.alternar();
    fixture.detectChanges();

    expect(emitido).toEqual([]);
    expect(botao().getAttribute('aria-pressed')).toBe('false');
    expect(botao().disabled).toBe(false);
  });

  it('deve ignorar cliques repetidos enquanto uma chamada está em andamento', () => {
    const pendente = new Subject<void>();
    seguidorServiceMock.seguirUsuario.mockReturnValue(pendente.asObservable());
    configurar('usuario', 2, false);

    component.alternar();
    component.alternar();
    fixture.detectChanges();

    expect(botao().disabled).toBe(true);
    expect(seguidorServiceMock.seguirUsuario).toHaveBeenCalledOnce();

    pendente.next();
    pendente.complete();
    fixture.detectChanges();

    expect(botao().disabled).toBe(false);
  });

  it('deve reagir a cliques reais no botão', () => {
    configurar('usuario', 2, false);

    botao().click();
    fixture.detectChanges();

    expect(seguidorServiceMock.seguirUsuario).toHaveBeenCalledWith(2);
    expect(botao().textContent).toContain('Seguindo');
  });
});
