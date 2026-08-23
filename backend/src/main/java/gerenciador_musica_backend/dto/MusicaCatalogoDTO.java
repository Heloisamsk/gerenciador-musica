package gerenciador_musica_backend.dto;

import java.util.List;

public record MusicaCatalogoDTO(
        Long idMusica,
        String titulo,
        Integer duracaoSegundos,
        Short anoLancamento,
        Long idArtistaPrincipal,
        String nomeArtistaPrincipal,
        Long idAlbum,
        String tituloAlbum,
        String capaUrl,
        List<String> generos,
        String papelArtista
) {
}
