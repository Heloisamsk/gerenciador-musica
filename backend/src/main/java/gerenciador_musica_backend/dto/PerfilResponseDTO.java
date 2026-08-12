package gerenciador_musica_backend.dto;

import java.time.OffsetDateTime;

public record PerfilResponseDTO(
        Long idUsuario,
        String username,
        String nome,
        OffsetDateTime dataCadastro,
        String fotoUrl
) {
}
