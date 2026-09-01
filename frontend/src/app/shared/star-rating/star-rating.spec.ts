import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StarRating } from './star-rating';

describe('StarRating', () => {
  let fixture: ComponentFixture<StarRating>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StarRating]
    }).compileComponents();

    fixture = TestBed.createComponent(StarRating);
  });

  it('deve preencher completamente as estrelas até a nota informada', () => {
    fixture.componentRef.setInput('nota', 3);
    fixture.detectChanges();

    const clipes = fixture.nativeElement.querySelectorAll(
      '.estrela-preenchida'
    );

    const larguras = Array.from(clipes as NodeListOf<SVGPathElement>).map(
      (path: SVGPathElement) => {
        const clipId = path.getAttribute('clip-path')?.match(/#([\w-]+)/)?.[1];
        const rect = fixture.nativeElement.querySelector(`#${clipId} rect`);
        return Number(rect?.getAttribute('width'));
      }
    );

    expect(larguras).toEqual([24, 24, 24, 0, 0]);
  });

  it('deve preencher meia estrela quando a nota tem .5', () => {
    fixture.componentRef.setInput('nota', 3.5);
    fixture.detectChanges();

    const clipes = fixture.nativeElement.querySelectorAll(
      '.estrela-preenchida'
    );
    const quartaEstrela = clipes[3] as SVGPathElement;
    const clipId = quartaEstrela.getAttribute('clip-path')?.match(/#([\w-]+)/)?.[1];
    const rect = fixture.nativeElement.querySelector(`#${clipId} rect`);

    expect(Number(rect?.getAttribute('width'))).toBe(12);
  });

  it('deve emitir nota inteira ao clicar na metade direita da estrela', () => {
    fixture.componentRef.setInput('interativo', true);
    fixture.detectChanges();

    let notaEmitida: number | undefined;
    fixture.componentInstance.notaEscolhida.subscribe(
      (nota: number) => (notaEmitida = nota)
    );

    const quartaEstrela = fixture.nativeElement.querySelectorAll(
      '.star-rating__estrela'
    )[3];
    const metadeDireita = quartaEstrela.querySelector(
      '.star-rating__metade--direita'
    ) as HTMLButtonElement;
    metadeDireita.click();

    expect(notaEmitida).toBe(4);
  });

  it('deve emitir nota com meia estrela ao clicar na metade esquerda', () => {
    fixture.componentRef.setInput('interativo', true);
    fixture.detectChanges();

    let notaEmitida: number | undefined;
    fixture.componentInstance.notaEscolhida.subscribe(
      (nota: number) => (notaEmitida = nota)
    );

    const quartaEstrela = fixture.nativeElement.querySelectorAll(
      '.star-rating__estrela'
    )[3];
    const metadeEsquerda = quartaEstrela.querySelector(
      '.star-rating__metade--esquerda'
    ) as HTMLButtonElement;
    metadeEsquerda.click();

    expect(notaEmitida).toBe(3.5);
  });

  it('não deve emitir nota quando não é interativo', () => {
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.star-rating__metade')
    ).toBeNull();
  });
});
