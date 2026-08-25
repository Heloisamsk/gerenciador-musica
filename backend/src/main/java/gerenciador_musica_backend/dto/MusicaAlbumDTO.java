package gerenciador_musica_backend.dto;

import java.util.List;

public record MusicaAlbumDTO(
        Long idMusica,
        String titulo,
        Integer duracaoSegundos,
        List<String> generos
) {
}
