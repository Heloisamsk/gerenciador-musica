package gerenciador_musica_backend.dto;

public record ArtistaCatalogoResumoDTO(
        Long idArtista,
        String nome,
        String nomeCompleto,
        String descricao,
        String fotoPerfilUrl,
        Long totalAlbuns,
        Long totalMusicasPrincipais,
        Long totalParticipacoes,
        Long duracaoTotalSegundos
) {
}
