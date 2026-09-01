package gerenciador_musica_backend.dto;

import java.util.List;

public record BuscaResultadoDTO(
        List<MusicaListagemDTO> musicas,
        List<AlbumResponseDTO> albuns,
        List<ArtistaResponseDTO> artistas
) {
}
