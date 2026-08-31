package gerenciador_musica_backend.exception;

public class ReviewJaExisteException extends RuntimeException {

    public ReviewJaExisteException(String mensagem) {
        super(mensagem);
    }
}
