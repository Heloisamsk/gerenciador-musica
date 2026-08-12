package gerenciador_musica_backend.exception;

public class PlaylistNaoEncontradaException extends RuntimeException {
    public PlaylistNaoEncontradaException(String message) {
        super(message);
    }
}
