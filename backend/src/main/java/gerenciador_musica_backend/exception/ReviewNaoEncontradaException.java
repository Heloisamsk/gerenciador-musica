package gerenciador_musica_backend.exception;

public class ReviewNaoEncontradaException extends RuntimeException {

    public ReviewNaoEncontradaException(Long idReview) {
        super("Review não encontrada com o ID: " + idReview);
    }
}
