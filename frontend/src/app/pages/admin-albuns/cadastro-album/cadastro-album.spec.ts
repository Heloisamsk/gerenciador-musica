import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { CadastroAlbum } from './cadastro-album';

describe('CadastroAlbum', () => {
  let component: CadastroAlbum;
  let fixture: ComponentFixture<CadastroAlbum>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CadastroAlbum, HttpClientTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(CadastroAlbum);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
