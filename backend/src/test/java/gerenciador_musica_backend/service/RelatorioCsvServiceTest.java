package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.RelatorioAlbumDTO;
import gerenciador_musica_backend.dto.RelatorioArtistaDTO;
import gerenciador_musica_backend.dto.RelatorioCatalogoDTO;
import gerenciador_musica_backend.dto.ResumoCatalogoDTO;
import gerenciador_musica_backend.dto.TipoRelatorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioCsvServiceTest {

    @Mock
    private RelatorioService relatorioService;

    @InjectMocks
    private RelatorioCsvService relatorioCsvService;

    @Test
    void deveExportarArtistasComAcentosAspasEFormulaNeutralizada() {
        RelatorioArtistaDTO artista = new RelatorioArtistaDTO(
                1L,
                "=Artista \"Teste\"",
                2,
                10,
                3,
                2400
        );
        when(relatorioService.gerarRelatorioCatalogo())
                .thenReturn(montarRelatorio(
                        List.of(artista),
                        List.of()
                ));

        String csv = new String(
                relatorioCsvService.exportar(TipoRelatorio.ARTISTAS),
                StandardCharsets.UTF_8
        );

        assertThat(csv)
                .startsWith("\uFEFF")
                .contains("\"Músicas principais\"")
                .contains("\"'=Artista \"\"Teste\"\"\"")
                .contains("\"2400\"");
    }

    @Test
    void deveExportarAlbunsComCabecalhoEConteudo() {
        RelatorioAlbumDTO album = new RelatorioAlbumDTO(
                1L,
                "A Night at the Opera",
                "Queen",
                (short) 1975,
                12,
                2580
        );
        when(relatorioService.gerarRelatorioCatalogo())
                .thenReturn(montarRelatorio(
                        List.of(),
                        List.of(album)
                ));

        String csv = new String(
                relatorioCsvService.exportar(TipoRelatorio.ALBUNS),
                StandardCharsets.UTF_8
        );

        assertThat(csv)
                .contains("\"Ano de lançamento\"")
                .contains("\"A Night at the Opera\";\"Queen\"")
                .contains("\"1975\";\"12\";\"2580\"");
    }

    private RelatorioCatalogoDTO montarRelatorio(
            List<RelatorioArtistaDTO> artistas,
            List<RelatorioAlbumDTO> albuns
    ) {
        return new RelatorioCatalogoDTO(
                OffsetDateTime.parse("2026-08-28T12:00:00Z"),
                new ResumoCatalogoDTO(1, 1, 1, 0, 100),
                artistas,
                albuns
        );
    }
}
