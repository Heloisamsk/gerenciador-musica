import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';

import type { AlbumDetalhe } from '../../models/AlbumDetalhe';
import type { Review } from '../../models/Review';
import { CatalogoService } from '../../services/catalogo';
import { ReviewService } from '../../services/review';
import { DetalheCatalogoBase } from '../../shared/detalhe-catalogo';
import { formatarDuracao } from '../../shared/formatar-duracao';
import { CurtirBotao } from '../../shared/curtir-botao/curtir-botao';

@Component({
  selector: 'app-album-detalhe',
  imports: [RouterLink, CurtirBotao],
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

  // Minha review deste álbum, se existir — troca o botão "Avaliar"
  // por "Ver minha review" e evita tentar criar uma duplicada.
  readonly minhaReview = signal<Review | null>(null);

  constructor(
    catalogoService: CatalogoService,
    private readonly reviewService: ReviewService,
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
    const idRota = this.route.snapshot.paramMap.get('id');
    this.inicializar(idRota);

    const id = Number(idRota);

    if (Number.isInteger(id) && id > 0) {
      this.reviewService.listarPorAlbum(id)
        .pipe(catchError(() => of(null)))
        .subscribe(pagina => {
          const minha = pagina?.itens.find(review => review.minhaReview);
          this.minhaReview.set(minha ?? null);
        });
    }
  }
}
