package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.dto.RelatorioAlbumDTO;
import gerenciador_musica_backend.dto.RelatorioArtistaDTO;
import gerenciador_musica_backend.dto.ResumoCatalogoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RelatorioRepositoryIntegrationTest {

    @Autowired
    private RelatorioRepository relatorioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveConsolidarOResumoPelasViewsDoCatalogo() {
        ResumoCatalogoDTO resumo =
                relatorioRepository.buscarResumoCatalogo();

        assertThat(resumo.totalArtistas())
                .isEqualTo(contarRegistros("artista"));
        assertThat(resumo.totalAlbuns())
                .isEqualTo(contarRegistros("album"));
        assertThat(resumo.totalMusicas())
                .isEqualTo(contarRegistros("musica"));
        assertThat(resumo.duracaoTotalSegundos()).isPositive();
    }

    @Test
    void deveListarArtistasEAlbunsComDadosAgregados() {
        List<RelatorioArtistaDTO> artistas =
                relatorioRepository.listarArtistas();
        List<RelatorioAlbumDTO> albuns =
                relatorioRepository.listarAlbuns();

        assertThat(artistas)
                .hasSize(contarRegistros("artista").intValue())
                .allSatisfy(artista -> {
                    assertThat(artista.idArtista()).isPositive();
                    assertThat(artista.nome()).isNotBlank();
                    assertThat(artista.totalAlbuns()).isNotNegative();
                    assertThat(artista.totalMusicasPrincipais())
                            .isNotNegative();
                });

        assertThat(albuns)
                .hasSize(contarRegistros("album").intValue())
                .allSatisfy(album -> {
                    assertThat(album.idAlbum()).isPositive();
                    assertThat(album.titulo()).isNotBlank();
                    assertThat(album.nomeArtista()).isNotBlank();
                    assertThat(album.totalMusicas()).isNotNegative();
                });
    }

    private Long contarRegistros(String tabela) {
        return switch (tabela) {
            case "artista" -> consultarContagem("SELECT COUNT(*) FROM artista");
            case "album" -> consultarContagem("SELECT COUNT(*) FROM album");
            case "musica" -> consultarContagem("SELECT COUNT(*) FROM musica");
            default -> throw new IllegalArgumentException("Tabela não permitida.");
        };
    }

    private Long consultarContagem(String consulta) {
        return jdbcTemplate.queryForObject(consulta, Long.class);
    }
}
