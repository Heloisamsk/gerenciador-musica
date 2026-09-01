import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import type {
  PerfilItem,
  PerfilResponse,
  TipoDestaquePerfil
} from '../../models/Perfil';
import { PerfilService } from '../../services/perfil';
import { SeguirBotao } from '../../shared/seguir-botao/seguir-botao';

@Component({
  selector: 'app-perfil-publico',
  standalone: true,
  imports: [RouterLink, SeguirBotao],
  templateUrl: './perfil-publico.html',
  styleUrls: ['../perfil/perfil.css']
})
export class PerfilPublico implements OnInit {

  readonly perfil = signal<PerfilResponse | null>(null);
  readonly carregando = signal(true);
  readonly mensagemErro = signal('');

  readonly destaquePrincipal = computed<PerfilItem | null>(() => {
    const perfil = this.perfil();
    if (!perfil) return null;

    return {
      ARTISTA: perfil.artistaDestaque,
      MUSICA: perfil.musicaDestaque,
      ALBUM: perfil.albumDestaque
    }[perfil.tipoDestaquePrincipal ?? 'ARTISTA'] ?? null;
  });

  readonly gruposFavoritos = computed(() => {
    const perfil = this.perfil();
    return [
      {
        tipo: 'ARTISTA' as const,
        titulo: 'Artistas',
        itens: perfil?.artistasFavoritos ?? []
      },
      {
        tipo: 'ALBUM' as const,
        titulo: 'Álbuns',
        itens: perfil?.albunsFavoritos ?? []
      },
      {
        tipo: 'MUSICA' as const,
        titulo: 'Músicas',
        itens: perfil?.musicasFavoritas ?? []
      }
    ];
  });

  constructor(
    private readonly perfilService: PerfilService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const idUsuario = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isInteger(idUsuario) || idUsuario <= 0) {
      this.carregando.set(false);
      this.mensagemErro.set('O identificador do perfil é inválido.');
      return;
    }

    this.perfilService.obterPorId(idUsuario).subscribe({
      next: perfil => {
        this.perfil.set(perfil);
        this.carregando.set(false);
      },
      error: erro => {
        this.carregando.set(false);
        this.mensagemErro.set(this.extrairMensagemErro(erro));
      }
    });
  }

  rotaItem(item: PerfilItem): string[] {
    const segmento = {
      ARTISTA: 'artistas',
      MUSICA: 'musicas',
      ALBUM: 'albuns'
    }[item.tipo];

    return ['/', segmento, String(item.id)];
  }

  rotuloTipo(tipo: TipoDestaquePerfil): string {
    return {
      ARTISTA: 'Artista em destaque',
      MUSICA: 'Música em destaque',
      ALBUM: 'Álbum em destaque'
    }[tipo];
  }

  imagemAlternativa(tipo: TipoDestaquePerfil): string {
    return tipo === 'ARTISTA' ? '/avatar-artista.png' : '/capa-padrao.png';
  }

  corrigirImagem(evento: Event, fallback: string): void {
    const imagem = evento.currentTarget as HTMLImageElement;
    if (!imagem.src.endsWith(fallback)) {
      imagem.src = fallback;
    }
  }

  private extrairMensagemErro(erro: unknown): string {
    if (erro instanceof HttpErrorResponse) {
      return erro.error?.message ?? 'Não foi possível carregar o perfil.';
    }
    return 'Não foi possível carregar o perfil.';
  }
}
