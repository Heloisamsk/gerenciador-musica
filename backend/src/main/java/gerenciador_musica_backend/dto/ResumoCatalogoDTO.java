package gerenciador_musica_backend.dto;

public record ResumoCatalogoDTO(
        long totalArtistas,
        long totalAlbuns,
        long totalMusicas,
        long totalParticipacoes,
        long duracaoTotalSegundos
) {
}
