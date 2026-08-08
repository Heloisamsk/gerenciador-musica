import { TestBed } from '@angular/core/testing';

import { AdminUsuario } from './admin-usuario';

describe('AdminUsuario', () => {
  let service: AdminUsuario;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AdminUsuario);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
