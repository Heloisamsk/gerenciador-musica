import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../environments/environment';
import { RelatorioCatalogo } from '../models/RelatorioCatalogo';
import { AdminRelatorioService } from './admin-relatorio';

describe('AdminRelatorioService', () => {
  let service: AdminRelatorioService;
  let httpMock: HttpTestingController;

  const apiUrl = `${environment.apiUrl}/api/admin/relatorios`;
  const relatorio: RelatorioCatalogo = {
    geradoEm: '2026-08-28T12:00:00Z',
    resumo: {
      totalArtistas: 1,
      totalAlbuns: 1,
      totalMusicas: 2,
      totalParticipacoes: 1,
      duracaoTotalSegundos: 420,
    },
    artistas: [],
    albuns: [],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AdminRelatorioService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AdminRelatorioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve obter o relatório consolidado do catálogo', () => {
    service.gerarCatalogo().subscribe((resposta) => {
      expect(resposta).toEqual(relatorio);
    });

    const requisicao = httpMock.expectOne(`${apiUrl}/catalogo`);

    expect(requisicao.request.method).toBe('GET');
    requisicao.flush(relatorio);
  });

  it.each(['ARTISTAS', 'ALBUNS'] as const)('deve exportar o relatório %s como Blob', (tipo) => {
    const arquivo = new Blob(['dados'], { type: 'text/csv' });

    service.exportarCatalogo(tipo).subscribe((resposta) => {
      expect(resposta).toEqual(arquivo);
    });

    const requisicao = httpMock.expectOne(
      (requisicaoHttp) =>
        requisicaoHttp.url === `${apiUrl}/catalogo.csv` &&
        requisicaoHttp.params.get('tipo') === tipo,
    );

    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.responseType).toBe('blob');
    requisicao.flush(arquivo);
  });
});
