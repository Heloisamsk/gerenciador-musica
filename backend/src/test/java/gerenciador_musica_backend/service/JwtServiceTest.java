package gerenciador_musica_backend.service;

import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * Teste de UNIDADE simples: aqui não há dependências para mockar,
 * então testamos o comportamento real de gerar e validar o token.
 */
class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

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

        Claims claims = jwtService.validarEExtrairClaims(token);

        assertThat(claims.getSubject()).isEqualTo("joao@email.com");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("nome", String.class)).isEqualTo("João");
    }

    @Test
    void deveLancarExcecaoAoValidarTokenInvalido() {
        assertThatThrownBy(() -> jwtService.validarEExtrairClaims("token-invalido"))
                .isInstanceOf(JwtException.class);
    }
}