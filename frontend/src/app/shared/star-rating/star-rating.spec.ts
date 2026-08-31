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

  it('deve preencher somente as estrelas até a nota informada', () => {
    fixture.componentRef.setInput('nota', 3);
    fixture.detectChanges();

    const preenchidas = fixture.nativeElement.querySelectorAll(
      '.estrela-preenchida'
    );

    expect(preenchidas.length).toBe(3);
  });

  it('deve emitir a nota escolhida ao clicar em modo interativo', () => {
    fixture.componentRef.setInput('interativo', true);
    fixture.detectChanges();

    let notaEmitida: number | undefined;
    fixture.componentInstance.notaEscolhida.subscribe(
      (nota: number) => (notaEmitida = nota)
    );

    const botoes = fixture.nativeElement.querySelectorAll(
      '.star-rating__botao'
    );
    (botoes[3] as HTMLButtonElement).click();

    expect(notaEmitida).toBe(4);
  });

  it('não deve emitir nota quando não é interativo', () => {
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.star-rating__botao')
    ).toBeNull();
  });
});
