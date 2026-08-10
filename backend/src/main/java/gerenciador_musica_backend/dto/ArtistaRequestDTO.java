package gerenciador_musica_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArtistaRequestDTO(

        @NotBlank(message = "O nome do artista é obrigatório")
        @Size(
                max = 255,
                message = "O nome do artista deve possuir no máximo 255 caracteres"
        )
        String nome,

        @Size(
                max = 500,
                message = "A descrição deve possuir no máximo 500 caracteres"
        )
        String descricao,

        @Size(
                max = 2048,
                message = "A URL da foto deve possuir no máximo 2048 caracteres"
        )
        String fotoPerfilUrl
) {
}