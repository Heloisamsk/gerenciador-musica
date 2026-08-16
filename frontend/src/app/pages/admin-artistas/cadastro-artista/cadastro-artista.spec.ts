import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CadastroArtista } from './cadastro-artista';

describe('CadastroArtista', () => {
  let component: CadastroArtista;
  let fixture: ComponentFixture<CadastroArtista>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CadastroArtista],
    }).compileComponents();

    fixture = TestBed.createComponent(CadastroArtista);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
