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
public class ArtistaExceptionHandler {

    /*
     * 404 - Artista não encontrado.
     */
    @ExceptionHandler(ArtistaNaoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarArtistaNaoEncontrado(
            ArtistaNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * 409 - Tentativa de cadastrar Artista já existente.
     */
    @ExceptionHandler(ArtistaDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarArtistaDuplicado(
            ArtistaDuplicadoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * 409 - Artista possui álbuns ou músicas associados.
     */
    @ExceptionHandler(ArtistaEmUsoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarArtistaEmUso(
            ArtistaEmUsoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * 400 - Dados da artista inválidos segundo
     * as regras de negócio do ArtistaService.
     */
    @ExceptionHandler(DadosArtistaInvalidosException.class)
    public ResponseEntity<ErrorResponseDTO> tratarDadosArtistaInvalidos(
            DadosArtistaInvalidosException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
