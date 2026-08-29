package gerenciador_musica_backend.dto;

import gerenciador_musica_backend.model.TipoDestaquePerfil;

public record PerfilItemResponseDTO(
        TipoDestaquePerfil tipo,
        Long id,
        String titulo,
        String subtitulo,
        String imagemUrl
) {
}
