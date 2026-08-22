import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { ArtistaRequest } from '../../../models/ArtistaRequest';
import { AdminArtistaService } from '../../../services/admin-artista';
import { FormularioArtista } from '../formulario-artista/formulario-artista';
import { CadastroArtista } from './cadastro-artista';

describe('CadastroArtista', () => {
  let component: CadastroArtista;
  let fixture: ComponentFixture<CadastroArtista>;
  const cadastrar = vi.fn();

  const artistaRequest: ArtistaRequest = {
    nome: 'Queen',
    nomeCompleto: 'Queen',
    descricao: 'Banda britânica de rock.',
    fotoPerfilUrl: null
  };

  beforeEach(async () => {
    cadastrar.mockReturnValue(of({
      idArtista: 52,
      ...artistaRequest
    }));

    await TestBed.configureTestingModule({
      imports: [CadastroArtista],
      providers: [
        {
          provide: AdminArtistaService,
          useValue: { cadastrar }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CadastroArtista);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar a tela com o formulário reutilizável', () => {
    expect(component).toBeTruthy();
    expect(fixture.debugElement.query(
      By.directive(FormularioArtista)
    )).toBeTruthy();
  });

  it('deve cadastrar o request emitido e limpar o formulário', () => {
    const formulario = fixture.debugElement.query(
      By.directive(FormularioArtista)
    ).componentInstance as FormularioArtista;
    formulario.formulario.setValue({
      ...artistaRequest,
      fotoPerfilUrl: ''
    });

    formulario.submeter();

    expect(cadastrar).toHaveBeenCalledWith(artistaRequest);
    expect(component.mensagemSucesso())
      .toBe('Artista Queen cadastrado com sucesso!');
    expect(component.carregando()).toBe(false);
    expect(formulario.formulario.getRawValue()).toEqual({
      nome: '',
      nomeCompleto: '',
      descricao: '',
      fotoPerfilUrl: ''
    });
  });
});
