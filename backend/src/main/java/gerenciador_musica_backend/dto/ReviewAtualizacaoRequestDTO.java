package gerenciador_musica_backend.dto;

import java.math.BigDecimal;

public record ReviewAtualizacaoRequestDTO(
        BigDecimal nota,
        String texto
) {
}
