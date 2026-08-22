import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { AdminPainel } from './admin-painel';

describe('AdminPainel', () => {
  let fixture: ComponentFixture<AdminPainel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminPainel],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminPainel);
    fixture.detectChanges();
  });

  it('deve criar o painel administrativo', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('deve exibir as seis opções administrativas', () => {
    const elemento = fixture.nativeElement as HTMLElement;
    const opcoes = Array.from(
      elemento.querySelectorAll<HTMLAnchorElement>('.acao-card')
    );

    expect(opcoes).toHaveLength(6);
    expect(opcoes.map(opcao => opcao.textContent?.trim())).toEqual([
      expect.stringContaining('Cadastrar música'),
      expect.stringContaining('Cadastrar artista'),
      expect.stringContaining('Cadastrar álbum'),
      expect.stringContaining('Listar músicas'),
      expect.stringContaining('Listar usuários'),
      expect.stringContaining('Listar artistas')
    ]);
  });

  it('deve direcionar cada opção para a tela correspondente', () => {
    const elemento = fixture.nativeElement as HTMLElement;
    const destinos = Array.from(
      elemento.querySelectorAll<HTMLAnchorElement>('.acao-card')
    ).map(opcao => opcao.getAttribute('href'));

    expect(destinos).toEqual([
      '/admin/banco/musicas/nova',
      '/admin/banco/artistas/novo',
      '/admin/banco/albuns/novo',
      '/admin/banco/musicas',
      '/admin/banco/usuarios',
      '/admin/banco/artistas'
    ]);
  });
});
