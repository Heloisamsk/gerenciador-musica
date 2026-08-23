import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditarMusica } from './editar-musica';

describe('EditarMusica', () => {
  let component: EditarMusica;
  let fixture: ComponentFixture<EditarMusica>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditarMusica]
    }).compileComponents();

    fixture = TestBed.createComponent(EditarMusica);
    component = fixture.componentInstance;
  });

  it('deve criar o componente de rota', () => {
    expect(component).toBeTruthy();
  });
});
