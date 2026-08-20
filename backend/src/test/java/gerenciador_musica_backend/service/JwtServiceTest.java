package gerenciador_musica_backend.service;

import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

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
    }

    @Test
    void deveLancarExcecaoAoValidarTokenInvalido() {
        assertThatThrownBy(() ->
                jwtService.validarEExtrairClaims(
                        "token-invalido"
                )
        ).isInstanceOf(JwtException.class);
    }
}