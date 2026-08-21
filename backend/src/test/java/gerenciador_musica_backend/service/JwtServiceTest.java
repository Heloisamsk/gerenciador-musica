package gerenciador_musica_backend.service;

import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final long EXPIRACAO_TESTE_MS = 86_400_000L;

    private final JwtService jwtService =
            new JwtService(
                    gerarChaveTeste(),
                    EXPIRACAO_TESTE_MS
            );

    private static String gerarChaveTeste() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);

        return Base64.getEncoder().encodeToString(bytes);
    }

    @Test
    void deveGerarTokenComClaimsCorretasEConseguirValidaLo() {
        Usuario usuario = new Usuario(
                "João",
                "joao@email.com",
                "senha-hash",
                Role.ADMIN
        );

        String token = jwtService.gerarToken(usuario);

        assertThat(token).isNotBlank();

        Claims claims =
                jwtService.validarEExtrairClaims(token);

        assertThat(claims.getSubject())
                .isEqualTo("joao@email.com");

        assertThat(claims.get("role", String.class))
                .isEqualTo("ADMIN");

        assertThat(claims.get("nome", String.class))
                .isEqualTo("João");

        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration())
                .isAfter(claims.getIssuedAt());
    }

    @Test
    void deveLancarExcecaoAoValidarTokenInvalido() {
        assertThatThrownBy(() ->
                jwtService.validarEExtrairClaims(
                        "token-invalido"
                )
        ).isInstanceOf(JwtException.class);
    }

    @Test
    void deveRejeitarChaveNula() {
        assertThatThrownBy(() ->
                new JwtService(
                        null,
                        EXPIRACAO_TESTE_MS
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A chave JWT não foi configurada.");
    }

    @Test
    void deveRejeitarChaveVazia() {
        assertThatThrownBy(() ->
                new JwtService(
                        "   ",
                        EXPIRACAO_TESTE_MS
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A chave JWT não foi configurada.");
    }

    @Test
    void deveRejeitarChaveComMenosDeTrintaEDoisBytes() {
        assertThatThrownBy(() ->
                new JwtService(
                        "chave-curta",
                        EXPIRACAO_TESTE_MS
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "A chave JWT deve possuir pelo menos 32 bytes."
                );
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void deveRejeitarTempoDeExpiracaoNaoPositivo(
            long expirationMs
    ) {
        String chaveTeste = gerarChaveTeste();

        assertThatThrownBy(() ->
                new JwtService(
                        chaveTeste,
                        expirationMs
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "O tempo de expiração do JWT deve ser positivo."
                );
    }
}
