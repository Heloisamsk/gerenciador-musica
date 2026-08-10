package gerenciador_musica_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlbumRequestDTO(

        @NotBlank(message = "O título do álbum é obrigatório")
        @Size(
                max = 255,
                message = "O título do álbum deve possuir no máximo 255 caracteres"
        )
        String titulo,

        @NotNull(message = "O ano de lançamento do álbum é obrigatório")
        @Min(
                value = 1800,
                message = "O ano do álbum deve ser no mínimo 1800"
        )
        @Max(
                value = 2100,
                message = "O ano do álbum deve ser no máximo 2100"
        )
        Short anoLancamento,

        @Size(
                max = 2048,
                message = "A URL da capa deve possuir no máximo 2048 caracteres"
        )
        String capaUrl
) {
}