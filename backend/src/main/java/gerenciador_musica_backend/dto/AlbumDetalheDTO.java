package gerenciador_musica_backend.dto;

import java.util.List;

public record AlbumDetalheDTO(
        AlbumCatalogoDTO album,
        List<String> generos,
        List<MusicaAlbumDTO> musicas
) {
}
