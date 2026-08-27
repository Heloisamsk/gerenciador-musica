import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Perfil } from './perfil';
import { AuthService } from '../../services/auth';
import { vi } from 'vitest';

describe('Perfil', () => {
  let component: Perfil;
  let fixture: ComponentFixture<Perfil>;

  beforeEach(async () => {
    const authServiceMock = {};

    await TestBed.configureTestingModule({
      imports: [Perfil],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Perfil);
    component = fixture.componentInstance;

    vi.spyOn(Storage.prototype, 'getItem').mockImplementation((key: string) => {
      if (key === 'nome') return 'Usuário Teste';
      if (key === 'email') return 'teste@teste.com';
      if (key === 'role') return 'USER';
      return null;
    });

    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
