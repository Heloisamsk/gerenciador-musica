package gerenciador_musica_backend.config;

import java.io.IOException;
import java.util.List;

import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            RestAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        /*
         * Se não houver token, o filtro continua normalmente.
         *
         * Caso o endpoint seja protegido, o próprio Spring Security
         * chamará o RestAuthenticationEntryPoint e retornará 401.
         *
         * Isso também permite acessar /api/auth/login e
         * /api/auth/register sem token.
         */
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorization.substring(7).trim();

        if (token.isBlank()) {
            responderNaoAutorizado(
                    request,
                    response,
                    new BadCredentialsException(
                            "Token não informado."
                    )
            );
            return;
        }

        try {
            Claims claims =
                    jwtService.validarEExtrairClaims(token);

            String email = claims.getSubject();

            if (email == null || email.isBlank()) {
                responderNaoAutorizado(
                        request,
                        response,
                        new BadCredentialsException(
                                "Token sem identificação do usuário."
                        )
                );
                return;
            }

            /*
             * Só cria uma autenticação se ainda não existir
             * uma no contexto desta requisição.
             */
            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                Usuario usuario = usuarioRepository
                        .findByEmail(email)
                        .orElse(null);

                if (usuario == null) {
                    responderNaoAutorizado(
                            request,
                            response,
                            new BadCredentialsException(
                                    "Usuário do token não encontrado."
                            )
                    );
                    return;
                }

                List<SimpleGrantedAuthority> authorities =
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_"
                                                + usuario
                                                .getRole()
                                                .name()
                                )
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContext context =
                        SecurityContextHolder
                                .createEmptyContext();

                context.setAuthentication(authentication);

                SecurityContextHolder.setContext(context);
            }

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException exception) {
            responderNaoAutorizado(
                    request,
                    response,
                    new BadCredentialsException(
                            "Token inválido ou expirado.",
                            exception
                    )
            );
        }
    }

    private void responderNaoAutorizado(
            HttpServletRequest request,
            HttpServletResponse response,
            BadCredentialsException exception
    ) throws IOException {

        /*
         * Garante que nenhuma autenticação parcial ou antiga
         * permaneça associada à requisição.
         */
        SecurityContextHolder.clearContext();

        /*
         * Produz o mesmo JSON padronizado definido no
         * RestAuthenticationEntryPoint.
         */
        authenticationEntryPoint.commence(
                request,
                response,
                exception
        );
    }
}