import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import type { AlbumDetalhe } from '../../models/AlbumDetalhe';
import { CatalogoService } from '../../services/catalogo';
import { DetalheCatalogoBase } from '../../shared/detalhe-catalogo';
import { formatarDuracao } from '../../shared/formatar-duracao';

@Component({
  selector: 'app-album-detalhe',
  imports: [RouterLink],
  templateUrl: './album-detalhe.html',
  styleUrls: [
    '../../shared/detalhe-catalogo.css',
    './album-detalhe.css'
  ]
})
export class AlbumDetalhePage
  extends DetalheCatalogoBase<AlbumDetalhe>
  implements OnInit {

  readonly idAlbum = this.idRecurso;
  readonly formatarDuracao = formatarDuracao;

  constructor(
    catalogoService: CatalogoService,
    private readonly route: ActivatedRoute
  ) {
    super(
      id => catalogoService.buscarDetalhesAlbum(id),
      {
        idInvalido: 'O identificador do álbum é inválido.',
        acessoNegado: 'Você não tem permissão para consultar este álbum.',
        naoEncontrado: 'Álbum não encontrado.',
        falhaCarregamento:
          'Não foi possível carregar o álbum. Tente novamente.'
      }
    );
  }

  ngOnInit(): void {
    this.inicializar(this.route.snapshot.paramMap.get('id'));
  }
}
