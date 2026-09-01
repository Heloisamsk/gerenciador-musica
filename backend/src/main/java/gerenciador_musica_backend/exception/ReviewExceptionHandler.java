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
public class ReviewExceptionHandler {

    @ExceptionHandler(ReviewNaoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> tratarReviewNaoEncontrada(
            ReviewNaoEncontradaException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DadosReviewInvalidosException.class)
    public ResponseEntity<ErrorResponseDTO> tratarDadosReviewInvalidos(
            DadosReviewInvalidosException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ReviewJaExisteException.class)
    public ResponseEntity<ErrorResponseDTO> tratarReviewJaExiste(
            ReviewJaExisteException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ReviewAcessoNegadoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarReviewAcessoNegado(
            ReviewAcessoNegadoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
