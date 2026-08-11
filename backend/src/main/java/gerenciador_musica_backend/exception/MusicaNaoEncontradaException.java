package gerenciador_musica_backend.exception;

public class MusicaNaoEncontradaException extends RuntimeException {
    public MusicaNaoEncontradaException(Long id) {
        super("Música não encontrada com o ID: " + id);
    }
}
