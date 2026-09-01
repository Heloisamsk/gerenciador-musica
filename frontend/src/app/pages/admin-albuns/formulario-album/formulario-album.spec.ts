import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { AlbumResponse } from '../../../models/AlbumResponse';
import { ArtistaResponse } from '../../../models/ArtistaResponse';
import { FormularioAlbum } from './formulario-album';

describe('FormularioAlbum', () => {
  let component: FormularioAlbum;
  let fixture: ComponentFixture<FormularioAlbum>;

  const artista: ArtistaResponse = {
    idArtista: 7,
    nome: 'Queen',
    nomeCompleto: 'Queen',
    descricao: 'Banda britânica de rock.',
    fotoPerfilUrl: null
  };

  const album: AlbumResponse = {
    idAlbum: 10,
    titulo: 'A Night at the Opera',
    anoLancamento: 1975,
    capaUrl: 'https://example.com/capa.jpg',
    artista: {
      id: artista.idArtista,
      nome: artista.nome,
      nomeCompleto: artista.nomeCompleto,
      descricao: artista.descricao,
      fotoPerfilUrl: artista.fotoPerfilUrl
    },
    curtida: false
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioAlbum]
    }).compileComponents();

    fixture = TestBed.createComponent(FormularioAlbum);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('artistas', [artista]);
    fixture.detectChanges();
  });

  it('deve usar os textos e permitir selecionar artista no cadastro', () => {
    const elemento = fixture.nativeElement as HTMLElement;

    expect(elemento.querySelector('h2')?.textContent)
      .toContain('Cadastrar álbum');
    expect(elemento.querySelector('.formulario-btn')?.textContent)
      .toContain('Cadastrar álbum');
    expect(elemento.querySelector('select')).not.toBeNull();
    expect(elemento.querySelector('input[readonly]')).toBeNull();
    expect(elemento.querySelectorAll('select option')).toHaveLength(2);
  });

  it('deve rejeitar título obrigatório ou formado apenas por espaços', () => {
    component.formulario.controls.titulo.setValue('   ');

    expect(component.formulario.controls.titulo
      .hasError('apenasEspacos')).toBe(true);

    component.formulario.controls.titulo.setValue('');

    expect(component.formulario.controls.titulo
      .hasError('required')).toBe(true);
  });

  it('deve validar os limites do título e da URL', () => {
    component.formulario.controls.titulo.setValue('a'.repeat(256));
    component.formulario.controls.capaUrl.setValue('b'.repeat(2049));

    expect(component.formulario.controls.titulo
      .hasError('maxlength')).toBe(true);
    expect(component.formulario.controls.capaUrl
      .hasError('maxlength')).toBe(true);
  });

  it('deve exigir ano entre 1800 e 2100', () => {
    component.formulario.controls.anoLancamento.setValue(null);
    expect(component.formulario.controls.anoLancamento
      .hasError('required')).toBe(true);

    component.formulario.controls.anoLancamento.setValue(1799);
    expect(component.formulario.controls.anoLancamento
      .hasError('min')).toBe(true);

    component.formulario.controls.anoLancamento.setValue(2101);
    expect(component.formulario.controls.anoLancamento
      .hasError('max')).toBe(true);
  });

  it('deve aceitar URL vazia e validar URL HTTP ou HTTPS', () => {
    component.formulario.controls.capaUrl.setValue('');
    expect(component.formulario.controls.capaUrl.valid).toBe(true);

    component.formulario.controls.capaUrl.setValue('url-invalida');
    expect(component.formulario.controls.capaUrl
      .hasError('urlInvalida')).toBe(true);

    component.formulario.controls.capaUrl.setValue(
      'ftp://example.com/capa.jpg'
    );
    expect(component.formulario.controls.capaUrl
      .hasError('urlInvalida')).toBe(true);

    component.formulario.controls.capaUrl.setValue(
      'https://example.com/capa.jpg'
    );
    expect(component.formulario.controls.capaUrl.valid).toBe(true);
  });

  it('deve normalizar e emitir AlbumRequest no cadastro', () => {
    const enviarCadastro = vi.fn();
    component.enviarCadastro.subscribe(enviarCadastro);
    component.formulario.setValue({
      titulo: '  A Night   at the Opera  ',
      idArtista: artista.idArtista,
      anoLancamento: 1975,
      capaUrl: '   '
    });

    component.submeter();

    expect(enviarCadastro).toHaveBeenCalledWith({
      titulo: 'A Night at the Opera',
      idArtista: 7,
      anoLancamento: 1975,
      capaUrl: null
    });
  });

  it('não deve emitir no cadastro quando o formulário for inválido', () => {
    const enviarCadastro = vi.fn();
    component.enviarCadastro.subscribe(enviarCadastro);

    component.submeter();

    expect(enviarCadastro).not.toHaveBeenCalled();
    expect(component.formulario.controls.titulo.touched).toBe(true);
    expect(component.formulario.controls.idArtista.touched).toBe(true);
    expect(component.formulario.controls.anoLancamento.touched)
      .toBe(true);
  });

  it('deve preencher os dados e deixar o artista somente leitura na edição', () => {
    fixture.componentRef.setInput('modo', 'edicao');
    fixture.componentRef.setInput('dadosIniciais', album);
    fixture.detectChanges();

    const elemento = fixture.nativeElement as HTMLElement;
    const artistaReadonly = elemento.querySelector<HTMLInputElement>(
      '#artistaResponsavel'
    );

    expect(component.formulario.getRawValue()).toEqual({
      titulo: album.titulo,
      idArtista: album.artista.id,
      anoLancamento: album.anoLancamento,
      capaUrl: album.capaUrl
    });
    expect(elemento.querySelector('h2')?.textContent)
      .toContain('Editar álbum');
    expect(elemento.querySelector('.formulario-btn')?.textContent)
      .toContain('Salvar alterações');
    expect(elemento.querySelector('select')).toBeNull();
    expect(artistaReadonly?.readOnly).toBe(true);
    expect(artistaReadonly?.value).toBe('Queen');
  });

  it('deve emitir atualização sem incluir idArtista', () => {
    const enviarEdicao = vi.fn();
    component.enviarEdicao.subscribe(enviarEdicao);
    fixture.componentRef.setInput('modo', 'edicao');
    fixture.componentRef.setInput('dadosIniciais', album);
    fixture.detectChanges();
    component.formulario.patchValue({
      titulo: '  Álbum   remasterizado  ',
      anoLancamento: 2011,
      capaUrl: ''
    });

    component.submeter();

    expect(enviarEdicao).toHaveBeenCalledWith({
      titulo: 'Álbum remasterizado',
      anoLancamento: 2011,
      capaUrl: null
    });
    expect(enviarEdicao.mock.calls[0][0].idArtista)
      .toBeUndefined();
  });

  it('deve bloquear envio duplicado enquanto estiver carregando', () => {
    const enviarEdicao = vi.fn();
    component.enviarEdicao.subscribe(enviarEdicao);
    fixture.componentRef.setInput('modo', 'edicao');
    fixture.componentRef.setInput('dadosIniciais', album);
    fixture.componentRef.setInput('carregando', true);
    fixture.detectChanges();

    component.submeter();

    expect(enviarEdicao).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>('.formulario-btn')?.disabled)
      .toBe(true);
  });

  it('deve emitir cancelamento e exibir mensagens acessíveis', () => {
    const cancelar = vi.fn();
    component.cancelar.subscribe(cancelar);
    fixture.componentRef.setInput('exibirCancelar', true);
    fixture.componentRef.setInput(
      'mensagemSucesso',
      'Álbum salvo com sucesso.'
    );
    fixture.componentRef.setInput(
      'mensagemErro',
      'Não foi possível salvar o álbum.'
    );
    fixture.detectChanges();

    const elemento = fixture.nativeElement as HTMLElement;
    const botaoCancelar = elemento.querySelector<HTMLButtonElement>(
      '.cancelar-btn'
    );
    botaoCancelar?.click();

    expect(cancelar).toHaveBeenCalledOnce();

    fixture.componentRef.setInput('carregando', true);
    fixture.detectChanges();

    expect(elemento.querySelectorAll(
      'output[aria-live="polite"]'
    ).length).toBeGreaterThanOrEqual(2);
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('Não foi possível salvar o álbum.');
  });
});
