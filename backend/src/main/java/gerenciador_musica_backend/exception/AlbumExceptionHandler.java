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
public class AlbumExceptionHandler {

    @ExceptionHandler(AlbumDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarAlbumDuplicado(
            AlbumDuplicadoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AlbumNaoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarAlbumNaoEncontrado(
            AlbumNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AlbumEmUsoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarAlbumEmUso(
            AlbumEmUsoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DadosAlbumInvalidosException.class)
    public ResponseEntity<ErrorResponseDTO> tratarDadosAlbumInvalidos(
            DadosAlbumInvalidosException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
