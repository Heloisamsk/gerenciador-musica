import {
  Component,
  input,
  output
} from '@angular/core';

import { ArtistaRequest } from '../../../models/ArtistaRequest';
import { FormularioArtista } from '../formulario-artista/formulario-artista';

@Component({
  selector: 'app-editar-artista',
  imports: [FormularioArtista],
  templateUrl: './editar-artista.html'
})
export class EditarArtista {

  readonly dadosIniciais = input<ArtistaRequest | null>(null);
  readonly carregando = input(false);
  readonly mensagemSucesso = input('');
  readonly mensagemErro = input('');

  readonly salvar = output<ArtistaRequest>();
}
