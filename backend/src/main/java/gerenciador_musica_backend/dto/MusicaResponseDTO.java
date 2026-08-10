package gerenciador_musica_backend.dto;

import java.util.Set;

public record MusicaResponseDTO(
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
}