package gerenciador_musica_backend.exception;

public class ArtistaDuplicadoException extends RuntimeException {
    public ArtistaDuplicadoException(String message) {
        super(message);
    }
}
