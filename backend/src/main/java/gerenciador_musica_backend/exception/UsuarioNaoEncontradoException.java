package gerenciador_musica_backend.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {

  public UsuarioNaoEncontradoException(Long id) {
    super("Usuário não encontrado com o ID: " + id);
  }
}
