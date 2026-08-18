package gerenciador_musica_backend.exception;

public class AlbumDuplicadoException extends RuntimeException {
    public AlbumDuplicadoException(String message) {
        super(message);
    }
}
