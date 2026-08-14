package gerenciador_musica_backend.dto;

public record ArtistaResponseDTO(
        Long idArtista,
        String nome,
        String nomeCompleto,
        String descricao,
        String fotoPerfilUrl
) {
}
