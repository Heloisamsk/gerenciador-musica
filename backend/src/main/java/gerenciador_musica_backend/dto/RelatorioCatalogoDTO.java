package gerenciador_musica_backend.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record RelatorioCatalogoDTO(
        OffsetDateTime geradoEm,
        ResumoCatalogoDTO resumo,
        List<RelatorioArtistaDTO> artistas,
        List<RelatorioAlbumDTO> albuns
) {
    public RelatorioCatalogoDTO {
        artistas = List.copyOf(artistas);
        albuns = List.copyOf(albuns);
    }
}
