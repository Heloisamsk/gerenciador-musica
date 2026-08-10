package gerenciador_musica_backend.dto;

public record AlbumResumoDTO(
        Long id,
        String titulo,
        Short anoLancamento,
        String capaUrl
) {
}