import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import type { AlbumResponse } from '../../../models/AlbumResponse';
import type { ArtistaResponse } from '../../../models/ArtistaResponse';
import type { MusicaResponse } from '../../../models/MusicaResponse';
import { AdminAlbumService } from '../../../services/admin-album.service';
import { AdminArtistaService } from '../../../services/admin-artista';
import { FormularioMusica } from './formulario-musica';

describe('FormularioMusica', () => {
  let component: FormularioMusica;
  let fixture: ComponentFixture<FormularioMusica>;

  const listarArtistas = vi.fn();
  const listarAlbunsPorArtista = vi.fn();

  const artistaPrincipal: ArtistaResponse = {
    idArtista: 7,
    nome: 'Artista Principal',
    nomeCompleto: 'Nome do Artista Principal',
    descricao: 'Descrição de teste.',
    fotoPerfilUrl: null
  };

  const artistaParticipante: ArtistaResponse = {
    idArtista: 8,
    nome: 'Artista Participante',
    nomeCompleto: 'Nome do Artista Participante',
    descricao: 'Descrição de teste.',
    fotoPerfilUrl: null
  };

  const outroArtista: ArtistaResponse = {
    idArtista: 9,
    nome: 'Outro Artista',
    nomeCompleto: 'Nome do Outro Artista',
    descricao: 'Descrição de teste.',
    fotoPerfilUrl: null
  };

  const album: AlbumResponse = {
    idAlbum: 11,
    titulo: 'Álbum de Teste',
    anoLancamento: 2024,
    capaUrl: null,
    artista: {
      id: artistaPrincipal.idArtista,
      nome: artistaPrincipal.nome,
      nomeCompleto: artistaPrincipal.nomeCompleto,
      descricao: artistaPrincipal.descricao,
      fotoPerfilUrl: null
    }
  };

  const musica: MusicaResponse = {
    id: 31,
    titulo: 'Música de Teste',
    letra: 'Texto original criado para o teste.',
    duracaoSegundos: 215,
    anoLancamento: 2024,
    artistaPrincipal: {
      id: artistaPrincipal.idArtista,
      nome: artistaPrincipal.nome
    },
    album: {
      id: album.idAlbum,
      titulo: album.titulo,
      anoLancamento: album.anoLancamento,
      capaUrl: null
    },
    artistasParticipantes: [
      {
        id: artistaParticipante.idArtista,
        nome: artistaParticipante.nome
      }
    ],
    generos: [
      { id: 1, nome: 'Gênero Um' },
      { id: 2, nome: 'Gênero Dois' }
    ]
  };

  beforeEach(async () => {
    listarArtistas.mockReset();
    listarAlbunsPorArtista.mockReset();
    listarArtistas.mockReturnValue(of([
      artistaPrincipal,
      artistaParticipante,
      outroArtista
    ]));
    listarAlbunsPorArtista.mockImplementation((id: number) =>
      of(id === artistaPrincipal.idArtista ? [album] : [])
    );

    await TestBed.configureTestingModule({
      imports: [FormularioMusica],
      providers: [
        {
          provide: AdminArtistaService,
          useValue: { listarArtistas }
        },
        {
          provide: AdminAlbumService,
          useValue: { listarAlbunsPorArtista }
        }
      ]
    }).compileComponents();
  });

  function criarComponente(
    modo: 'cadastro' | 'edicao' = 'cadastro',
    dados: MusicaResponse | null = null
  ): void {
    fixture = TestBed.createComponent(FormularioMusica);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('modo', modo);
    fixture.componentRef.setInput('dadosIniciais', dados);
    fixture.detectChanges();
    fixture.detectChanges();
  }

  function preencherFormularioValido(
    albumId: number | null = album.idAlbum
  ): void {
    component.formulario.controls.artistaPrincipalId
      .setValue(artistaPrincipal.idArtista);
    component.formulario.patchValue({
      titulo: '  Música   Normalizada  ',
      letra: '  Texto criado para o teste.  ',
      duracaoSegundos: 240,
      anoLancamento: 2025,
      generosTexto: '  Gênero Um, Gênero   Dois ',
      artistasParticipantesIds: [artistaParticipante.idArtista],
      albumId
    });
  }

  it('deve carregar artistas e usar os textos do cadastro', () => {
    criarComponente();
    const elemento = fixture.nativeElement as HTMLElement;

    expect(listarArtistas).toHaveBeenCalledOnce();
    expect(elemento.querySelector('h1')?.textContent)
      .toContain('Cadastrar música');
    expect(elemento.querySelector('.formulario-btn')?.textContent)
      .toContain('Cadastrar música');
    expect(elemento.querySelector('#letra')).not.toBeNull();
    expect(elemento.querySelector('#artistasParticipantesIds'))
      .not.toBeNull();
  });

  it('deve validar título, duração, ano e gêneros', () => {
    criarComponente();

    component.formulario.patchValue({
      titulo: '   ',
      duracaoSegundos: 0,
      anoLancamento: 1799,
      generosTexto: 'Gênero, gênero'
    });

    expect(component.formulario.controls.titulo
      .hasError('apenasEspacos')).toBe(true);
    expect(component.formulario.controls.duracaoSegundos
      .hasError('min')).toBe(true);
    expect(component.formulario.controls.anoLancamento
      .hasError('min')).toBe(true);
    expect(component.formulario.controls.generosTexto
      .hasError('generoDuplicado')).toBe(true);

    component.formulario.patchValue({
      titulo: 'a'.repeat(256),
      anoLancamento: 2101,
      generosTexto: 'a'.repeat(101)
    });

    expect(component.formulario.controls.titulo
      .hasError('maxlength')).toBe(true);
    expect(component.formulario.controls.anoLancamento
      .hasError('max')).toBe(true);
    expect(component.formulario.controls.generosTexto
      .hasError('generoMuitoLongo')).toBe(true);
  });

  it('deve emitir um payload normalizado com todas as associações', () => {
    criarComponente();
    const enviar = vi.fn();
    component.enviar.subscribe(enviar);
    preencherFormularioValido();

    component.submeter();

    expect(enviar).toHaveBeenCalledWith({
      titulo: 'Música Normalizada',
      letra: 'Texto criado para o teste.',
      duracaoSegundos: 240,
      anoLancamento: 2025,
      artistaPrincipalId: 7,
      artistasParticipantesIds: [8],
      albumId: 11,
      generos: ['Gênero Um', 'Gênero Dois']
    });
  });

  it('deve permitir música sem letra, participantes ou álbum', () => {
    criarComponente();
    const enviar = vi.fn();
    component.enviar.subscribe(enviar);
    preencherFormularioValido(null);
    component.formulario.patchValue({
      letra: '   ',
      artistasParticipantesIds: []
    });

    component.submeter();

    expect(enviar.mock.calls[0][0]).toMatchObject({
      letra: null,
      artistasParticipantesIds: [],
      albumId: null
    });
  });

  it('não deve emitir quando o formulário estiver inválido', () => {
    criarComponente();
    const enviar = vi.fn();
    component.enviar.subscribe(enviar);

    component.submeter();

    expect(enviar).not.toHaveBeenCalled();
    expect(component.formulario.controls.titulo.touched).toBe(true);
    expect(component.formulario.controls.artistaPrincipalId.touched)
      .toBe(true);
  });

  it('deve remover o artista principal dos participantes', () => {
    criarComponente();
    component.formulario.controls.artistasParticipantesIds
      .setValue([
        artistaPrincipal.idArtista,
        artistaParticipante.idArtista
      ]);

    component.formulario.controls.artistaPrincipalId
      .setValue(artistaPrincipal.idArtista);

    expect(component.formulario.controls.artistasParticipantesIds.value)
      .toEqual([artistaParticipante.idArtista]);
    expect(component.artistasParticipantesDisponiveis())
      .not.toContain(artistaPrincipal);
  });

  it('deve carregar somente álbuns do principal e limpar o álbum ao trocá-lo', () => {
    criarComponente();
    component.formulario.controls.artistaPrincipalId
      .setValue(artistaPrincipal.idArtista);
    component.formulario.controls.albumId.setValue(album.idAlbum);

    component.formulario.controls.artistaPrincipalId
      .setValue(outroArtista.idArtista);

    expect(listarAlbunsPorArtista).toHaveBeenNthCalledWith(
      1,
      artistaPrincipal.idArtista
    );
    expect(listarAlbunsPorArtista).toHaveBeenNthCalledWith(
      2,
      outroArtista.idArtista
    );
    expect(component.formulario.controls.albumId.value).toBeNull();
    expect(component.albuns()).toEqual([]);
  });

  it('deve preencher todos os dados iniciais no modo de edição', () => {
    criarComponente('edicao', musica);
    const elemento = fixture.nativeElement as HTMLElement;

    expect(listarAlbunsPorArtista)
      .toHaveBeenCalledWith(artistaPrincipal.idArtista);
    expect(component.formulario.getRawValue()).toEqual({
      titulo: musica.titulo,
      letra: musica.letra,
      duracaoSegundos: musica.duracaoSegundos,
      anoLancamento: musica.anoLancamento,
      generosTexto: 'Gênero Um, Gênero Dois',
      artistaPrincipalId: artistaPrincipal.idArtista,
      artistasParticipantesIds: [artistaParticipante.idArtista],
      albumId: album.idAlbum
    });
    expect(elemento.querySelector('h1')?.textContent)
      .toContain('Editar música');
    expect(elemento.querySelector('.formulario-btn')?.textContent)
      .toContain('Salvar alterações');
  });

  it('deve informar falhas ao carregar artistas e álbuns', () => {
    listarArtistas.mockReturnValue(throwError(() => new Error('falha')));
    criarComponente();

    expect(component.erroArtistas())
      .toBe('Não foi possível carregar a lista de artistas.');

    listarArtistas.mockReturnValue(of([
      artistaPrincipal,
      artistaParticipante
    ]));
    listarAlbunsPorArtista.mockReturnValue(
      throwError(() => new Error('falha'))
    );
    criarComponente();
    component.formulario.controls.artistaPrincipalId
      .setValue(artistaPrincipal.idArtista);

    expect(component.erroAlbuns())
      .toBe('Não foi possível carregar os álbuns desse artista.');
  });

  it('deve bloquear envio duplicado e emitir cancelamento acessível', () => {
    criarComponente();
    preencherFormularioValido();
    const enviar = vi.fn();
    const cancelar = vi.fn();
    component.enviar.subscribe(enviar);
    component.cancelar.subscribe(cancelar);
    fixture.componentRef.setInput('carregando', true);
    fixture.componentRef.setInput('exibirCancelar', true);
    fixture.componentRef.setInput(
      'mensagemSucesso',
      'Música salva com sucesso.'
    );
    fixture.componentRef.setInput(
      'mensagemErro',
      'Não foi possível salvar a música.'
    );
    fixture.detectChanges();

    component.submeter();
    fixture.componentRef.setInput('carregando', false);
    fixture.detectChanges();
    const elemento = fixture.nativeElement as HTMLElement;
    elemento.querySelector<HTMLButtonElement>('.cancelar-btn')?.click();

    expect(enviar).not.toHaveBeenCalled();
    expect(cancelar).toHaveBeenCalledOnce();
    expect(elemento.querySelector('output[aria-live="polite"]')?.textContent)
      .toContain('Música salva com sucesso.');
    expect(elemento.querySelector('[role="alert"]')?.textContent)
      .toContain('Não foi possível salvar a música.');
  });
});
