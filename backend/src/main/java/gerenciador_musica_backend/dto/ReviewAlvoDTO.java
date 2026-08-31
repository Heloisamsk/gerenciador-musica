package gerenciador_musica_backend.dto;

import gerenciador_musica_backend.model.TipoAlvoReview;

public record ReviewAlvoDTO(
        TipoAlvoReview tipo,
        Long id,
        String titulo,
        String artista,
        String capaUrl
) {
}
