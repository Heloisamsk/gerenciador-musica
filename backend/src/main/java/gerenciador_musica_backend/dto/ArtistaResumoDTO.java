package gerenciador_musica_backend.dto;

public record ArtistaResumoDTO(
        Long id,
        String nome,
        String descricao,
        String fotoPerfilUrl
) {
}
