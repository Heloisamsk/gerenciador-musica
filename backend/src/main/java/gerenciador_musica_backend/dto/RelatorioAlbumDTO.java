package gerenciador_musica_backend.dto;

public record RelatorioAlbumDTO(
        Long idAlbum,
        String titulo,
        String nomeArtista,
        Short anoLancamento,
        long totalMusicas,
        long duracaoTotalSegundos
) {
}
