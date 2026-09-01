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
public class MusicaExceptionHandler {

    /*
     * 400 - Dados da música inválidos segundo
     * as regras de negócio do MusicaService.
     */
    @ExceptionHandler(DadosMusicaInvalidosException.class)
    public ResponseEntity<ErrorResponseDTO> tratarDadosMusicaInvalidos(
            DadosMusicaInvalidosException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * 404 - Música não encontrada.
     */
    @ExceptionHandler(MusicaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> tratarMusicaNaoEncontrada(
            MusicaNaoEncontradaException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * 409 - Tentativa de cadastrar ou atualizar uma música duplicada.
     */
    @ExceptionHandler(MusicaDuplicadaException.class)
    public ResponseEntity<ErrorResponseDTO> tratarMusicaDuplicada(
            MusicaDuplicadaException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
