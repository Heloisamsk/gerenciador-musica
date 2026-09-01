package gerenciador_musica_backend.exception;

public class SeguirUsuarioInvalidoException extends RuntimeException {

    public SeguirUsuarioInvalidoException(String mensagem) {
        super(mensagem);
    }
}