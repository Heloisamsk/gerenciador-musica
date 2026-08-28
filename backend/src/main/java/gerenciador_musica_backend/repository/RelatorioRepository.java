package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.dto.RelatorioAlbumDTO;
import gerenciador_musica_backend.dto.RelatorioArtistaDTO;
import gerenciador_musica_backend.dto.ResumoCatalogoDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RelatorioRepository {

    private static final String CONSULTA_RESUMO = """
            SELECT
                COUNT(*) AS total_artistas,
                COALESCE(SUM(total_albuns), 0) AS total_albuns,
                COALESCE(SUM(total_musicas_principais), 0) AS total_musicas,
                COALESCE(SUM(total_participacoes), 0) AS total_participacoes,
                COALESCE(SUM(duracao_total_segundos), 0)
                    AS duracao_total_segundos
            FROM vw_artista_resumo_catalogo
            """;

    private static final String CONSULTA_ARTISTAS = """
            SELECT
                id_artista,
                nome,
                total_albuns,
                total_musicas_principais,
                total_participacoes,
                duracao_total_segundos
            FROM vw_artista_resumo_catalogo
            ORDER BY
                total_musicas_principais DESC,
                total_albuns DESC,
                nome ASC,
                id_artista ASC
            """;

    private static final String CONSULTA_ALBUNS = """
            SELECT
                id_album,
                titulo,
                nome_artista,
                ano_lancamento,
                total_musicas,
                duracao_total_segundos
            FROM vw_albuns_artista_catalogo
            ORDER BY
                ano_lancamento DESC,
                titulo ASC,
                id_album ASC
            """;

    private final JdbcClient jdbcClient;

    public RelatorioRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public ResumoCatalogoDTO buscarResumoCatalogo() {
        return jdbcClient.sql(CONSULTA_RESUMO)
                .query((resultado, numeroLinha) -> new ResumoCatalogoDTO(
                        resultado.getLong("total_artistas"),
                        resultado.getLong("total_albuns"),
                        resultado.getLong("total_musicas"),
                        resultado.getLong("total_participacoes"),
                        resultado.getLong("duracao_total_segundos")
                ))
                .single();
    }

    public List<RelatorioArtistaDTO> listarArtistas() {
        return jdbcClient.sql(CONSULTA_ARTISTAS)
                .query((resultado, numeroLinha) -> new RelatorioArtistaDTO(
                        resultado.getLong("id_artista"),
                        resultado.getString("nome"),
                        resultado.getLong("total_albuns"),
                        resultado.getLong("total_musicas_principais"),
                        resultado.getLong("total_participacoes"),
                        resultado.getLong("duracao_total_segundos")
                ))
                .list();
    }

    public List<RelatorioAlbumDTO> listarAlbuns() {
        return jdbcClient.sql(CONSULTA_ALBUNS)
                .query((resultado, numeroLinha) -> new RelatorioAlbumDTO(
                        resultado.getLong("id_album"),
                        resultado.getString("titulo"),
                        resultado.getString("nome_artista"),
                        resultado.getShort("ano_lancamento"),
                        resultado.getLong("total_musicas"),
                        resultado.getLong("duracao_total_segundos")
                ))
                .list();
    }
}
