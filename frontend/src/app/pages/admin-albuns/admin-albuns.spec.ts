import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminAlbuns } from './admin-albuns';

describe('AdminAlbuns', () => {
  let component: AdminAlbuns;
  let fixture: ComponentFixture<AdminAlbuns>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminAlbuns]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminAlbuns);
    component = fixture.componentInstance;
  });

  it('deve criar o componente de rota', () => {
    expect(component).toBeTruthy();
  });
});
