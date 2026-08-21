package gerenciador_musica_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record MusicaRequestDTO(

        @NotBlank(message = "O título é obrigatório")
        @Size(
                max = 255,
                message = "O título deve possuir no máximo 255 caracteres"
        )
        String titulo,

        String letra,

        @NotNull(message = "A duração é obrigatória")
        @Positive(message = "A duração deve ser maior que zero")
        Integer duracaoSegundos,

        @NotNull(message = "O ano de lançamento é obrigatório")
        @Min(
                value = 1800,
                message = "O ano de lançamento deve ser no mínimo 1800"
        )
        @Max(
                value = 2100,
                message = "O ano de lançamento deve ser no máximo 2100"
        )
        Short anoLancamento,

        @NotNull(message = "O ID do artista principal é obrigatório")
        @Positive(message = "O ID do artista principal deve ser positivo")
        Long artistaPrincipalId,

        @NotNull(message = "Informe a lista de artistas participantes")
        Set<
                @NotNull(message = "O ID do participante não pode ser nulo")
                @Positive(message = "O ID do participante deve ser positivo")
                        Long
                > artistasParticipantesIds,

        @Positive(message = "O ID do álbum deve ser positivo")
        Long albumId,

        @NotNull(message = "Informe os gêneros da música")
        @Size(
                min = 1,
                message = "A música precisa possuir pelo menos um gênero"
        )
        Set<
                @NotBlank(message = "O nome do gênero não pode ser vazio")
                @Size(
                        max = 100,
                        message = "O gênero deve possuir no máximo 100 caracteres"
                )
                        String
                > generos
) {
}
