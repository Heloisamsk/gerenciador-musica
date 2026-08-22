import { ComponentFixture, TestBed } from '@angular/core/testing';

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
  });

  it('deve criar o componente de rota', () => {
    expect(component).toBeTruthy();
  });
});
