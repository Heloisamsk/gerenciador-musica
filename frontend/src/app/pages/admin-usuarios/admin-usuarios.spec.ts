import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { AdminUsuarios } from './admin-usuarios';

describe('AdminUsuarios', () => {
  let component: AdminUsuarios;
  let fixture: ComponentFixture<AdminUsuarios>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminUsuarios],
      providers: [provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminUsuarios);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
