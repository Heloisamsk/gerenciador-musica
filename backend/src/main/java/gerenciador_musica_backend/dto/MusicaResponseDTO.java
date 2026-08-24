package gerenciador_musica_backend.dto;

import java.util.Set;

/**
 * Representação completa de uma música, usada como DTO de detalhes (US06):
 * inclui letra e participantes, além dos dados já presentes em {@link MusicaListagemDTO}.
 */
public record MusicaResponseDTO(
        Long id,
        String titulo,
        String letra,
        Integer duracaoSegundos,
        Short anoLancamento,
        ArtistaResumoDTO artistaPrincipal,
        AlbumResumoDTO album,
        Set<ArtistaResumoDTO> artistasParticipantes,
        Set<GeneroResumoDTO> generos,
        String youtubeVideoId
) {

    public MusicaResponseDTO(
            Long id,
            String titulo,
            String letra,
            Integer duracaoSegundos,
            Short anoLancamento,
            ArtistaResumoDTO artistaPrincipal,
            AlbumResumoDTO album,
            Set<ArtistaResumoDTO> artistasParticipantes,
            Set<GeneroResumoDTO> generos
    ) {
        this(
                id,
                titulo,
                letra,
                duracaoSegundos,
                anoLancamento,
                artistaPrincipal,
                album,
                artistasParticipantes,
                generos,
                null
        );
    }
}
