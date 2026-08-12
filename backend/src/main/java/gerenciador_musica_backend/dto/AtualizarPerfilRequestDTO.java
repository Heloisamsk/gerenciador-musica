package gerenciador_musica_backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequestDTO(

        @Size(
                min = 3,
                max = 30,
                message = "O username deve possuir entre 3 e 30 caracteres."
        )
        @Pattern(
                regexp = "^[A-Za-z0-9._]+$",
                message = "O username pode conter apenas letras, números, ponto e underline."
        )
        String username,

        @Size(
                max = 2048,
                message = "A URL da foto deve possuir no máximo 2048 caracteres."
        )
        String fotoUrl
) {
}
