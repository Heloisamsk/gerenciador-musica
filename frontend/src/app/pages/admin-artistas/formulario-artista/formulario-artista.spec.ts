import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { ArtistaRequest } from '../../../models/ArtistaRequest';
import { FormularioArtista } from './formulario-artista';

describe('FormularioArtista', () => {
  let component: FormularioArtista;
  let fixture: ComponentFixture<FormularioArtista>;

  const dadosValidos: ArtistaRequest = {
    nome: 'Queen',
    nomeCompleto: 'Queen',
    descricao: 'Banda britânica de rock.',
    fotoPerfilUrl: 'https://exemplo.com/queen.jpg'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioArtista]
    }).compileComponents();

    fixture = TestBed.createComponent(FormularioArtista);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve usar os textos do modo de cadastro', () => {
    const elemento = fixture.nativeElement as HTMLElement;

    expect(elemento.querySelector('h2')?.textContent)
      .toContain('Cadastrar artista');
    expect(elemento.querySelector('button')?.textContent)
      .toContain('Cadastrar artista');
  });

  it('deve exigir nome artístico, nome completo e descrição', () => {
    component.formulario.patchValue({
      nome: '   ',
      nomeCompleto: '   ',
      descricao: '   '
    });

    expect(component.formulario.controls.nome
      .hasError('apenasEspacos')).toBe(true);
    expect(component.formulario.controls.nomeCompleto
      .hasError('apenasEspacos')).toBe(true);
    expect(component.formulario.controls.descricao
      .hasError('apenasEspacos')).toBe(true);
  });

  it('deve validar os limites de todos os campos', () => {
    component.formulario.setValue({
      nome: 'a'.repeat(256),
      nomeCompleto: 'b'.repeat(256),
      descricao: 'c'.repeat(501),
      fotoPerfilUrl: 'd'.repeat(2049)
    });

    expect(component.formulario.controls.nome
      .hasError('maxlength')).toBe(true);
    expect(component.formulario.controls.nomeCompleto
      .hasError('maxlength')).toBe(true);
    expect(component.formulario.controls.descricao
      .hasError('maxlength')).toBe(true);
    expect(component.formulario.controls.fotoPerfilUrl
      .hasError('maxlength')).toBe(true);
  });

  it('deve permitir que a URL da foto fique vazia', () => {
    component.formulario.setValue({
      ...dadosValidos,
      fotoPerfilUrl: ''
    });

    expect(component.formulario.valid).toBe(true);
  });

  it('não deve emitir quando o formulário for inválido', () => {
    const enviar = vi.fn();
    component.enviar.subscribe(enviar);

    component.submeter();

    expect(enviar).not.toHaveBeenCalled();
    expect(component.formulario.controls.nome.touched).toBe(true);
    expect(component.formulario.controls.nomeCompleto.touched)
      .toBe(true);
    expect(component.formulario.controls.descricao.touched)
      .toBe(true);
  });

  it('deve normalizar os valores e converter URL vazia para null', () => {
    const enviar = vi.fn();
    component.enviar.subscribe(enviar);
    component.formulario.setValue({
      nome: '  Queen   +   Adam Lambert  ',
      nomeCompleto: '  Queen   e Adam Lambert  ',
      descricao: '  Projeto   musical em atividade.  ',
      fotoPerfilUrl: '   '
    });

    component.submeter();

    expect(enviar).toHaveBeenCalledWith({
      nome: 'Queen + Adam Lambert',
      nomeCompleto: 'Queen e Adam Lambert',
      descricao: 'Projeto musical em atividade.',
      fotoPerfilUrl: null
    });
  });

  it('deve normalizar uma URL preenchida antes de emitir', () => {
    const enviar = vi.fn();
    component.enviar.subscribe(enviar);
    component.formulario.setValue({
      ...dadosValidos,
      fotoPerfilUrl: '  https://exemplo.com/queen.jpg  '
    });

    component.submeter();

    expect(enviar).toHaveBeenCalledWith(dadosValidos);
  });

  it('deve preencher os dados iniciais e usar os textos de edição', () => {
    fixture.componentRef.setInput('modo', 'edicao');
    fixture.componentRef.setInput('dadosIniciais', dadosValidos);
    fixture.detectChanges();

    const elemento = fixture.nativeElement as HTMLElement;

    expect(component.formulario.getRawValue()).toEqual(dadosValidos);
    expect(elemento.querySelector('h2')?.textContent)
      .toContain('Editar artista');
    expect(elemento.querySelector('button')?.textContent)
      .toContain('Salvar alterações');
  });

  it('deve emitir o cancelamento quando a ação estiver disponível', () => {
    const cancelar = vi.fn();
    component.cancelar.subscribe(cancelar);
    fixture.componentRef.setInput('exibirCancelar', true);
    fixture.detectChanges();

    const botaoCancelar = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>('.cancelar-btn');
    botaoCancelar?.click();

    expect(botaoCancelar?.textContent).toContain('Cancelar');
    expect(cancelar).toHaveBeenCalledOnce();
  });

  it('deve exibir mensagens acessíveis de sucesso, erro e carregamento', () => {
    fixture.componentRef.setInput(
      'mensagemSucesso',
      'Artista salvo com sucesso.'
    );
    fixture.componentRef.setInput(
      'mensagemErro',
      'Não foi possível salvar o artista.'
    );
    fixture.componentRef.setInput('carregando', true);
    fixture.detectChanges();

    const elemento = fixture.nativeElement as HTMLElement;
    const mensagensPolidas = elemento.querySelectorAll(
      'output[aria-live="polite"]'
    );

    expect(mensagensPolidas.length).toBe(2);
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('Não foi possível salvar o artista.');
    expect(elemento.querySelector('button')?.textContent)
      .toContain('Cadastrando artista...');
  });
});
