package gerenciador_musica_backend.dto;

public record ReviewRequestDTO(
        Long idMusica,
        Long idAlbum,
        Short nota,
        String texto
) {
}
