import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AlbumResponse } from '../../../models/AlbumResponse';
import { ArtistaResponse } from '../../../models/ArtistaResponse';
import { AdminAlbumService } from '../../../services/admin-album.service';
import { AdminArtistaService } from '../../../services/admin-artista';
import { FormularioAlbum } from '../formulario-album/formulario-album';
import { CadastroAlbum } from './cadastro-album';

describe('CadastroAlbum', () => {
  let component: CadastroAlbum;
  let fixture: ComponentFixture<CadastroAlbum>;

  const listarArtistas = vi.fn();
  const cadastrarAlbum = vi.fn();

  const artista: ArtistaResponse = {
    idArtista: 1,
    nome: 'Queen',
    nomeCompleto: 'Queen',
    descricao: 'Banda britânica de rock.',
    fotoPerfilUrl: null
  };

  const album: AlbumResponse = {
    idAlbum: 10,
    titulo: 'A Night at the Opera',
    anoLancamento: 1975,
    capaUrl: null,
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
    listarArtistas.mockReset();
    cadastrarAlbum.mockReset();
    listarArtistas.mockReturnValue(of([artista]));
    cadastrarAlbum.mockReturnValue(of(album));

    await TestBed.configureTestingModule({
      imports: [CadastroAlbum],
      providers: [
        provideRouter([]),
        {
          provide: AdminAlbumService,
          useValue: { cadastrarAlbum }
        },
        {
          provide: AdminArtistaService,
          useValue: { listarArtistas }
        }
      ]
    }).compileComponents();
  });

  function criarComponente(): void {
    fixture = TestBed.createComponent(CadastroAlbum);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function obterFormulario(): FormularioAlbum {
    return fixture.debugElement.query(
      By.directive(FormularioAlbum)
    ).componentInstance as FormularioAlbum;
  }

  function preencherFormularioValido(
    formulario: FormularioAlbum
  ): void {
    formulario.formulario.setValue({
      titulo: '  A Night   at the Opera  ',
      idArtista: artista.idArtista,
      anoLancamento: 1975,
      capaUrl: '   '
    });
  }

  it('deve criar a tela usando o formulário reutilizável', () => {
    criarComponente();

    const formulario = obterFormulario();

    expect(component).toBeTruthy();
    expect(formulario.modo()).toBe('cadastro');
    expect(formulario.artistas()).toEqual([artista]);
    expect(listarArtistas).toHaveBeenCalledOnce();
  });

  it('deve informar quando não houver artistas cadastrados', () => {
    listarArtistas.mockReturnValue(of([]));

    criarComponente();

    expect(component.artistas).toEqual([]);
    expect(component.erroArtistas()).toBe(
      'Nenhum artista cadastrado. Cadastre um artista primeiro.'
    );
    expect(obterFormulario().cadastroIndisponivel()).toBe(true);
  });

  it('deve tratar falha ao carregar os artistas', () => {
    listarArtistas.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 500 })
    ));

    criarComponente();

    expect(component.artistas).toEqual([]);
    expect(component.erroArtistas()).toBe(
      'Não foi possível carregar a lista de artistas.'
    );
    expect(component.carregandoArtistas()).toBe(false);
  });

  it('deve cadastrar o request emitido e limpar o formulário', () => {
    criarComponente();
    const formulario = obterFormulario();
    preencherFormularioValido(formulario);

    formulario.submeter();

    expect(cadastrarAlbum).toHaveBeenCalledWith({
      titulo: 'A Night at the Opera',
      idArtista: 1,
      anoLancamento: 1975,
      capaUrl: null
    });
    expect(component.mensagemSucesso()).toBe(
      'Álbum A Night at the Opera cadastrado com sucesso!'
    );
    expect(component.carregando()).toBe(false);
    expect(formulario.formulario.getRawValue()).toEqual({
      titulo: '',
      idArtista: null,
      anoLancamento: null,
      capaUrl: ''
    });
  });

  it('deve bloquear cadastros duplicados durante a requisição', () => {
    const resposta = new Subject<AlbumResponse>();
    cadastrarAlbum.mockReturnValue(resposta.asObservable());
    criarComponente();
    const formulario = obterFormulario();
    preencherFormularioValido(formulario);

    formulario.submeter();
    formulario.submeter();

    expect(cadastrarAlbum).toHaveBeenCalledOnce();
    expect(component.carregando()).toBe(true);

    resposta.next(album);
    resposta.complete();

    expect(component.carregando()).toBe(false);
  });

  it.each([
    [0, {}, 'Não foi possível conectar ao servidor.'],
    [400, {}, 'Existem dados inválidos no formulário.'],
    [400, { message: 'Título inválido.' }, 'Título inválido.'],
    [401, {}, 'Sua sessão não é válida. Faça login novamente.'],
    [403, {}, 'Você não possui permissão para cadastrar álbuns.'],
    [404, {}, 'O artista selecionado não foi encontrado.'],
    [409, {}, 'Esse álbum já está cadastrado.'],
    [500, {}, 'Ocorreu um erro ao cadastrar o álbum.']
  ])(
    'deve tratar o erro HTTP %i no cadastro',
    (status, error, mensagem) => {
      cadastrarAlbum.mockReturnValue(throwError(() =>
        new HttpErrorResponse({ status, error })
      ));
      criarComponente();
      const formulario = obterFormulario();
      preencherFormularioValido(formulario);

      formulario.submeter();

      expect(component.mensagemErro()).toBe(mensagem);
      expect(component.mensagemSucesso()).toBe('');
      expect(component.carregando()).toBe(false);
      expect(formulario.formulario.controls.titulo.value)
        .toContain('A Night');
    }
  );
});
