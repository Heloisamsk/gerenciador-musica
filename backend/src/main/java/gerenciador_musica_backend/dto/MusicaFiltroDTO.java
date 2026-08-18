package gerenciador_musica_backend.dto;

/**
 * Filtros opcionais para a pesquisa de músicas (US06).
 * Todos os campos são opcionais; campos nulos são ignorados na busca.
 *
 * @param titulo         texto pesquisado no título da música (busca parcial, sem diferenciar maiúsculas/minúsculas)
 * @param artistaId      ID do artista principal para filtrar
 * @param albumId        ID do álbum para filtrar
 * @param generoId       ID do gênero para filtrar
 * @param anoLancamento  ano de lançamento exato para filtrar
 */
public record MusicaFiltroDTO(
        String titulo,
        Long artistaId,
        Long albumId,
        Long generoId,
        Short anoLancamento
) {
}
