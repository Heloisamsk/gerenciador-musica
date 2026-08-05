package gerenciador_musica_backend.exception;

public class EmailJaCadastradoException extends RuntimeException {

  public EmailJaCadastradoException(String mensagem) {
    super(mensagem);
  }
}