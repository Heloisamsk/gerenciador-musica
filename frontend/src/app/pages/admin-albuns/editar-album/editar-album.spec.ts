import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditarAlbum } from './editar-album';

describe('EditarAlbum', () => {
  let component: EditarAlbum;
  let fixture: ComponentFixture<EditarAlbum>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditarAlbum]
    }).compileComponents();

    fixture = TestBed.createComponent(EditarAlbum);
    component = fixture.componentInstance;
  });

  it('deve criar o componente de rota', () => {
    expect(component).toBeTruthy();
  });
});
