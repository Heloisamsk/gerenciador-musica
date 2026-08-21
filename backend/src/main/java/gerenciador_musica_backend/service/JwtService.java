package gerenciador_musica_backend.service;

import gerenciador_musica_backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class JwtService {

    private static final int TAMANHO_MINIMO_CHAVE = 32;

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "A chave JWT não foi configurada."
            );
        }

        byte[] keyBytes =
                secretKey.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < TAMANHO_MINIMO_CHAVE) {
            throw new IllegalStateException(
                    "A chave JWT deve possuir pelo menos 32 bytes."
            );
        }

        if (expirationMs <= 0) {
            throw new IllegalStateException(
                    "O tempo de expiração do JWT deve ser positivo."
            );
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String gerarToken(Usuario usuario) {
        Instant instanteAtual = Instant.now();
        Instant instanteExpiracao = instanteAtual.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("role", usuario.getRole().name())
                .claim("nome", usuario.getNome())
                .claim(Claims.ISSUED_AT, instanteAtual.getEpochSecond())
                .claim(Claims.EXPIRATION, instanteExpiracao.getEpochSecond())
                .signWith(key)
                .compact();
    }

    public Claims validarEExtrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
