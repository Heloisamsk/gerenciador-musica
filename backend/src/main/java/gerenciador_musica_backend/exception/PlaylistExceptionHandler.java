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
public class PlaylistExceptionHandler {

    /*
     * 403 - Usuário autenticado tentando acessar
     * playlist que não é dele.
     */
    @ExceptionHandler(PlaylistAcessoNegadoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarPlaylistAcessoNegado(
            PlaylistAcessoNegadoException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * 404 - Playlist não encontrada.
     */
    @ExceptionHandler(PlaylistNaoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> tratarPlaylistNaoEncontrada(
            PlaylistNaoEncontradaException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * 409 - Tentativa de renomear, trocar a capa, excluir ou
     * mexer diretamente na playlist especial "Favoritos".
     */
    @ExceptionHandler(PlaylistEspecialException.class)
    public ResponseEntity<ErrorResponseDTO> tratarPlaylistEspecial(
            PlaylistEspecialException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
