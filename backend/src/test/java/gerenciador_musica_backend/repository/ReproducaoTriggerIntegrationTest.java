package gerenciador_musica_backend.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReproducaoTriggerIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void devePermitirReproducaoDentroDaDuracaoDaMusica() {
        MusicaDuracao musica = buscarMusica();
        Long usuarioId = buscarUsuarioId();

        int registros = jdbcTemplate.update("""
                INSERT INTO reproducao (
                    id_usuario,
                    id_musica,
                    segundos_ouvidos
                ) VALUES (?, ?, ?)
                """,
                usuarioId,
                musica.id(),
                musica.duracaoSegundos()
        );

        assertThat(registros).isOne();
    }

    @Test
    void naoDevePermitirReproducaoMaiorQueADuracaoDaMusica() {
        MusicaDuracao musica = buscarMusica();
        Long usuarioId = buscarUsuarioId();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO reproducao (
                    id_usuario,
                    id_musica,
                    segundos_ouvidos
                ) VALUES (?, ?, ?)
                """,
                usuarioId,
                musica.id(),
                musica.duracaoSegundos() + 1
        ))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(
                        "não podem superar a duração da música"
                );
    }

    @Test
    void naoDevePermitirAtualizarReproducaoParaDuracaoInvalida() {
        MusicaDuracao musica = buscarMusica();
        Long usuarioId = buscarUsuarioId();
        Long reproducaoId = jdbcTemplate.queryForObject("""
                INSERT INTO reproducao (
                    id_usuario,
                    id_musica,
                    segundos_ouvidos
                ) VALUES (?, ?, 1)
                RETURNING id_reproducao
                """,
                Long.class,
                usuarioId,
                musica.id()
        );

        assertThatThrownBy(() -> jdbcTemplate.update("""
                UPDATE reproducao
                SET segundos_ouvidos = ?
                WHERE id_reproducao = ?
                """,
                musica.duracaoSegundos() + 1,
                reproducaoId
        ))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(
                        "não podem superar a duração da música"
                );
    }

    private MusicaDuracao buscarMusica() {
        return jdbcTemplate.queryForObject("""
                SELECT id_musica, duracao_segundos
                FROM musica
                ORDER BY id_musica
                LIMIT 1
                """,
                (resultado, numeroLinha) -> new MusicaDuracao(
                        resultado.getLong("id_musica"),
                        resultado.getInt("duracao_segundos")
                )
        );
    }

    private Long buscarUsuarioId() {
        return jdbcTemplate.queryForObject("""
                SELECT id
                FROM usuario
                ORDER BY id
                LIMIT 1
                """, Long.class);
    }

    private record MusicaDuracao(Long id, int duracaoSegundos) {
    }
}
