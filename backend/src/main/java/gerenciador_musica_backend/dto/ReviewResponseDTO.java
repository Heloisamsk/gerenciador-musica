package gerenciador_musica_backend.dto;

import java.time.OffsetDateTime;

public record ReviewResponseDTO(
        Long idReview,
        ReviewAutorDTO autor,
        ReviewAlvoDTO alvo,
        Short nota,
        String texto,
        OffsetDateTime criadaEm,
        OffsetDateTime atualizadaEm,
        boolean minhaReview
) {
}
