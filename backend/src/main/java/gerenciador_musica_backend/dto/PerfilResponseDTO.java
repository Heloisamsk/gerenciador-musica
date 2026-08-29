package gerenciador_musica_backend.dto;

import java.time.OffsetDateTime;

import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.TipoDestaquePerfil;

public record PerfilResponseDTO(
        Long idUsuario,
        String username,
        String nome,
        OffsetDateTime dataCadastro,
        Role role,
        String fotoUrl,
        String bannerUrl,
        String biografia,
        String fraseDestaque,
        TipoDestaquePerfil tipoDestaquePrincipal,
        PerfilItemResponseDTO artistaDestaque,
        PerfilItemResponseDTO musicaDestaque,
        PerfilItemResponseDTO albumDestaque
) {
}
