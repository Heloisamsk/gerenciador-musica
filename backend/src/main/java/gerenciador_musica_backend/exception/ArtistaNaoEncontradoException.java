package gerenciador_musica_backend.exception;

public class ArtistaNaoEncontradoException extends RuntimeException {

  public ArtistaNaoEncontradoException(Long id) {
    super("Artista não encontrado com o ID: " + id);
  }
}
