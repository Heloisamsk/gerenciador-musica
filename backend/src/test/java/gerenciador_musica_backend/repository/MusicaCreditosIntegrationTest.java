package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MusicaCreditosIntegrationTest {

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private MusicaRepository musicaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveNormalizarTodosOsDadosPovoadosSemDuplicarAView() {
        assertThat(contarColunaLegada()).isZero();
        assertThat(contarMusicasComQuantidadeInvalidaDePrincipais()).isZero();
        assertThat(contarMusicasLogicamenteDuplicadas()).isZero();
        assertThat(contarMusicasDuplicadasNaView()).isZero();
        assertThat(contarCreditosSemPapel()).isZero();
        assertThat(contarParticipacoesPovoadas()).isPositive();
    }

    @Test
    void devePersistirETrocarOsPapeisNaMesmaAssociacao() {
        Artista primeiroPrincipal = salvarArtista("Principal anterior");
        Artista novoPrincipal = salvarArtista("Principal novo");
        Musica musica = new Musica(
                "Música normalizada " + sufixoUnico(),
                null,
                180,
                (short) 2026,
                primeiroPrincipal,
                null
        );
        musica.setArtistasParticipantes(Set.of(novoPrincipal));
        musica = musicaRepository.saveAndFlush(musica);

        Long idMusica = musica.getIdMusica();
        assertThat(contarPapel(idMusica, "PRINCIPAL")).isEqualTo(1L);
        assertThat(contarPapel(idMusica, "FEAT")).isEqualTo(1L);

        musica.definirCreditosArtistas(
                novoPrincipal,
                Set.of(primeiroPrincipal)
        );
        musicaRepository.flush();
        entityManager.clear();

        Musica atualizada = musicaRepository.findById(idMusica).orElseThrow();

        assertThat(atualizada.getArtistaPrincipal().getIdArtista())
                .isEqualTo(novoPrincipal.getIdArtista());
        assertThat(atualizada.getArtistasParticipantes())
                .extracting(Artista::getIdArtista)
                .containsExactly(primeiroPrincipal.getIdArtista());
        assertThat(contarPapel(idMusica, "PRINCIPAL")).isEqualTo(1L);
        assertThat(contarPapel(idMusica, "FEAT")).isEqualTo(1L);
        assertThat(contarCreditos(idMusica)).isEqualTo(2L);
    }

    @Test
    void naoDevePermitirDoisArtistasPrincipaisNaMesmaMusica() {
        Artista principal = salvarArtista("Artista principal");
        Artista segundoPrincipal = salvarArtista("Segundo principal");
        Musica musica = musicaRepository.saveAndFlush(new Musica(
                "Música com principal único " + sufixoUnico(),
                null,
                180,
                (short) 2026,
                principal,
                null
        ));

        jdbcTemplate.update("""
                INSERT INTO musica_artista (
                    id_musica,
                    id_artista,
                    papel_participacao
                ) VALUES (?, ?, 'PRINCIPAL')
                """,
                musica.getIdMusica(),
                segundoPrincipal.getIdArtista()
        );

        assertThatThrownBy(() -> jdbcTemplate.execute(
                "SET CONSTRAINTS "
                        + "ct_credito_exige_artista_principal IMMEDIATE"
        ))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(
                        "deve possuir exatamente um artista principal"
                );
    }

    private Artista salvarArtista(String nomeBase) {
        String nome = nomeBase + " " + sufixoUnico();

        return artistaRepository.saveAndFlush(new Artista(
                nome,
                nome + " completo",
                "Descrição de teste.",
                null
        ));
    }

    private Long contarColunaLegada() {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name = 'musica'
                  AND column_name = 'id_artista'
                """, Long.class);
    }

    private Long contarMusicasComQuantidadeInvalidaDePrincipais() {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT musica.id_musica
                    FROM musica
                    LEFT JOIN musica_artista credito
                        ON credito.id_musica = musica.id_musica
                       AND credito.papel_participacao = 'PRINCIPAL'
                    GROUP BY musica.id_musica
                    HAVING COUNT(credito.id_artista) <> 1
                ) inconsistencias
                """, Long.class);
    }

    private Long contarMusicasDuplicadasNaView() {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT id_artista_contexto, id_musica
                    FROM vw_musicas_artista_catalogo
                    GROUP BY id_artista_contexto, id_musica
                    HAVING COUNT(*) > 1
                ) duplicidades
                """, Long.class);
    }

    private Long contarMusicasLogicamenteDuplicadas() {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT
                        musica.id_album,
                        CASE
                            WHEN musica.id_album IS NULL
                                THEN principal.id_artista
                            ELSE NULL
                        END AS id_artista_single,
                        LOWER(BTRIM(musica.titulo)) AS titulo,
                        CASE
                            WHEN musica.id_album IS NULL
                                THEN musica.ano_lancamento
                            ELSE NULL
                        END AS ano_single
                    FROM musica
                    JOIN musica_artista principal
                        ON principal.id_musica = musica.id_musica
                       AND principal.papel_participacao = 'PRINCIPAL'
                    GROUP BY
                        musica.id_album,
                        id_artista_single,
                        LOWER(BTRIM(musica.titulo)),
                        ano_single
                    HAVING COUNT(*) > 1
                ) duplicidades
                """, Long.class);
    }

    private Long contarCreditosSemPapel() {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM musica_artista
                WHERE papel_participacao IS NULL
                """, Long.class);
    }

    private Long contarParticipacoesPovoadas() {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM musica_artista
                WHERE papel_participacao = 'FEAT'
                """, Long.class);
    }

    private Long contarPapel(Long idMusica, String papel) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM musica_artista
                WHERE id_musica = ?
                  AND papel_participacao = ?
                """, Long.class, idMusica, papel);
    }

    private Long contarCreditos(Long idMusica) {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM musica_artista
                WHERE id_musica = ?
                """, Long.class, idMusica);
    }

    private static String sufixoUnico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
