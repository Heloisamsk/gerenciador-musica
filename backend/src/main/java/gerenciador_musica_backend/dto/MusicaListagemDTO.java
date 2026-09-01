package gerenciador_musica_backend.dto;

import java.util.Set;

/**
 * Representação resumida de uma música para os resultados da pesquisa (US06).
 * Reaproveita os DTOs de resumo já existentes; a URL da capa vem de {@link AlbumResumoDTO#capaUrl()}
 * quando a música possui álbum associado.
 *
 * @param id                  ID da música
 * @param titulo              título da música
 * @param duracaoSegundos     duração da música em segundos
 * @param anoLancamento       ano de lançamento da música
 * @param artistaPrincipal    resumo do artista principal
 * @param album               resumo do álbum (nulo quando a música não pertence a um álbum)
 * @param artistasParticipantes resumos dos artistas participantes
 * @param generos             resumo dos gêneros associados à música
 * @param curtida             se o usuário autenticado curtiu esta música
 */
public record MusicaListagemDTO(
        Long id,
        String titulo,
        Integer duracaoSegundos,
        Short anoLancamento,
        ArtistaResumoDTO artistaPrincipal,
        AlbumResumoDTO album,
        Set<ArtistaResumoDTO> artistasParticipantes,
        Set<GeneroResumoDTO> generos,
        boolean curtida
) {
}
