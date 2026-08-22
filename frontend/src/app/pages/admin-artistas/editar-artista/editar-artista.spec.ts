import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { vi } from 'vitest';

import { ArtistaRequest } from '../../../models/ArtistaRequest';
import { FormularioArtista } from '../formulario-artista/formulario-artista';
import { EditarArtista } from './editar-artista';

describe('EditarArtista', () => {
  let component: EditarArtista;
  let fixture: ComponentFixture<EditarArtista>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditarArtista]
    }).compileComponents();

    fixture = TestBed.createComponent(EditarArtista);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve usar o formulário reutilizável no modo de edição', () => {
    const dadosIniciais: ArtistaRequest = {
      nome: 'Queen',
      nomeCompleto: 'Queen',
      descricao: 'Banda britânica de rock.',
      fotoPerfilUrl: null
    };
    fixture.componentRef.setInput('dadosIniciais', dadosIniciais);
    fixture.detectChanges();

    const formulario = fixture.debugElement.query(
      By.directive(FormularioArtista)
    ).componentInstance as FormularioArtista;

    expect(component).toBeTruthy();
    expect(formulario.modo()).toBe('edicao');
    expect(formulario.formulario.getRawValue()).toEqual({
      ...dadosIniciais,
      fotoPerfilUrl: ''
    });
  });

  it('deve repassar o request válido emitido pelo formulário', () => {
    const request: ArtistaRequest = {
      nome: 'Queen Atualizado',
      nomeCompleto: 'Queen',
      descricao: 'Descrição atualizada.',
      fotoPerfilUrl: null
    };
    const salvar = vi.fn();
    component.salvar.subscribe(salvar);

    const formulario = fixture.debugElement.query(
      By.directive(FormularioArtista)
    ).componentInstance as FormularioArtista;
    formulario.formulario.setValue({
      ...request,
      fotoPerfilUrl: ''
    });
    formulario.submeter();

    expect(salvar).toHaveBeenCalledWith(request);
  });
});
