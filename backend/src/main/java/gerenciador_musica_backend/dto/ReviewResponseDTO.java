package gerenciador_musica_backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReviewResponseDTO(
        Long idReview,
        ReviewAutorDTO autor,
        ReviewAlvoDTO alvo,
        BigDecimal nota,
        String texto,
        OffsetDateTime criadaEm,
        OffsetDateTime atualizadaEm,
        boolean minhaReview
) {
}
