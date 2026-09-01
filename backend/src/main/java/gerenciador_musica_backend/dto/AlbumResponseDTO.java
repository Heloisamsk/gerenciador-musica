package gerenciador_musica_backend.dto;

public record AlbumResponseDTO(
        Long idAlbum,
        String titulo,
        Short anoLancamento,
        String capaUrl,
        ArtistaResumoDTO artista,
        boolean curtida
) {
}
