import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import type { ArtistaDetalhe } from '../../models/ArtistaDetalhe';
import { CatalogoService } from '../../services/catalogo';
import { DetalheCatalogoBase } from '../../shared/detalhe-catalogo';
import { formatarDuracao } from '../../shared/formatar-duracao';
import { SeguirBotao } from '../../shared/seguir-botao/seguir-botao';

@Component({
  selector: 'app-artista-detalhe',
  imports: [RouterLink, SeguirBotao],
  templateUrl: './artista-detalhe.html',
  styleUrls: [
    '../../shared/detalhe-catalogo.css',
    './artista-detalhe.css'
  ]
})
export class ArtistaDetalhePage
  extends DetalheCatalogoBase<ArtistaDetalhe>
  implements OnInit {

  readonly idArtista = this.idRecurso;
  readonly formatarDuracao = formatarDuracao;

  constructor(
    catalogoService: CatalogoService,
    private readonly route: ActivatedRoute
  ) {
    super(
      id => catalogoService.buscarDetalhesArtista(id),
      {
        idInvalido: 'O identificador do artista é inválido.',
        acessoNegado: 'Você não tem permissão para consultar este artista.',
        naoEncontrado: 'Artista não encontrado.',
        falhaCarregamento:
          'Não foi possível carregar o artista. Tente novamente.'
      }
    );
  }

  ngOnInit(): void {
    this.inicializar(this.route.snapshot.paramMap.get('id'));
  }

  rotuloPapel(papel: 'PRINCIPAL' | 'PARTICIPANTE'): string {
    return papel === 'PARTICIPANTE'
      ? 'Participação'
      : 'Artista principal';
  }

}
