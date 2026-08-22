import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminArtistas } from './admin-artistas';

describe('AdminArtistas', () => {
  let component: AdminArtistas;
  let fixture: ComponentFixture<AdminArtistas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminArtistas]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminArtistas);
    component = fixture.componentInstance;
  });

  it('deve criar o componente de rota', () => {
    expect(component).toBeTruthy();
  });
});
