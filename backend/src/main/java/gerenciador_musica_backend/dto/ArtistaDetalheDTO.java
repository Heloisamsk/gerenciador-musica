package gerenciador_musica_backend.dto;

import java.util.List;

public record ArtistaDetalheDTO(
        ArtistaCatalogoResumoDTO artista,
        List<AlbumCatalogoDTO> albuns,
        List<MusicaCatalogoDTO> musicas
) {
}
