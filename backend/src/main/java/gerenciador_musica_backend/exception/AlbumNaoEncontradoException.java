package gerenciador_musica_backend.exception;

public class AlbumNaoEncontradoException extends RuntimeException {

    public AlbumNaoEncontradoException(Long idAlbum) {
        super("Álbum não encontrado com o ID: " + idAlbum);
    }
}
