package gerenciador_musica_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import gerenciador_musica_backend.model.TipoDestaquePerfil;

import java.util.List;

public record AtualizarPerfilRequestDTO(

        @NotBlank(message = "O nome público é obrigatório.")
        @Size(
                max = 255,
                message = "O nome público deve possuir no máximo 255 caracteres."
        )
        String nome,

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
        @Pattern(
                regexp = "^(https?://.*)?$",
                message = "A URL da foto deve começar com http:// ou https://."
        )
        String fotoUrl,

        @Size(
                max = 2048,
                message = "A URL do banner deve possuir no máximo 2048 caracteres."
        )
        @Pattern(
                regexp = "^(https?://.*)?$",
                message = "A URL do banner deve começar com http:// ou https://."
        )
        String bannerUrl,

        @Size(
                max = 800,
                message = "A biografia deve possuir no máximo 800 caracteres."
        )
        String biografia,

        @Size(
                max = 160,
                message = "A frase de destaque deve possuir no máximo 160 caracteres."
        )
        String fraseDestaque,

        @Positive(message = "O artista selecionado é inválido.")
        Long idArtistaDestaque,

        @Positive(message = "A música selecionada é inválida.")
        Long idMusicaDestaque,

        @Positive(message = "O álbum selecionado é inválido.")
        Long idAlbumDestaque,

        TipoDestaquePerfil tipoDestaquePrincipal,

        @Size(
                max = 3,
                message = "Selecione no máximo três artistas favoritos."
        )
        List<@Positive(message = "O artista favorito é inválido.") Long>
                idsArtistasFavoritos,

        @Size(
                max = 3,
                message = "Selecione no máximo três álbuns favoritos."
        )
        List<@Positive(message = "O álbum favorito é inválido.") Long>
                idsAlbunsFavoritos,

        @Size(
                max = 3,
                message = "Selecione no máximo três músicas favoritas."
        )
        List<@Positive(message = "A música favorita é inválida.") Long>
                idsMusicasFavoritas
) {
}
