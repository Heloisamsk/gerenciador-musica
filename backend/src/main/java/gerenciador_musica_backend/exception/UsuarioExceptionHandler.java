package gerenciador_musica_backend.exception;

import gerenciador_musica_backend.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(0)
public class UsuarioExceptionHandler {

    @ExceptionHandler(DadosPerfilInvalidosException.class)
    public ResponseEntity<ErrorResponseDTO> tratarDadosPerfilInvalidos(
            DadosPerfilInvalidosException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * 404 - Usuário não encontrado (perfil público).
     */
    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarUsuarioNaoEncontrado(
            UsuarioNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * 409 - Tentativa de cadastrar e-mail já existente.
     */
    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarEmailJaCadastrado(
            EmailJaCadastradoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * 400 - Tentativa de seguir a si mesmo.
     */
    @ExceptionHandler(SeguirUsuarioInvalidoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarSeguirUsuarioInvalido(
            SeguirUsuarioInvalidoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
