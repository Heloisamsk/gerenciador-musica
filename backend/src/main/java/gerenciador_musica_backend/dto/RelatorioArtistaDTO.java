package gerenciador_musica_backend.dto;

public record RelatorioArtistaDTO(
        Long idArtista,
        String nome,
        long totalAlbuns,
        long totalMusicasPrincipais,
        long totalParticipacoes,
        long duracaoTotalSegundos
) {
}
