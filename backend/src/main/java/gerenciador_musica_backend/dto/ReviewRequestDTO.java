package gerenciador_musica_backend.dto;

import java.math.BigDecimal;

public record ReviewRequestDTO(
        Long idMusica,
        Long idAlbum,
        BigDecimal nota,
        String texto
) {
}
