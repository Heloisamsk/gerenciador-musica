package gerenciador_musica_backend.exception;

public class MusicaDuplicadaException extends RuntimeException {
    public MusicaDuplicadaException(String message) {
        super(message);
    }
}
