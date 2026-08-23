package gerenciador_musica_backend.dto;

public record AlbumCatalogoDTO(
        Long idAlbum,
        Long idArtista,
        String nomeArtista,
        String titulo,
        Short anoLancamento,
        String capaUrl,
        Long totalMusicas,
        Long duracaoTotalSegundos
) {
}
