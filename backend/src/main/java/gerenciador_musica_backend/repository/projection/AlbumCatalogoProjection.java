package gerenciador_musica_backend.repository.projection;

public interface AlbumCatalogoProjection {

    Long getIdAlbum();

    Long getIdArtista();

    String getNomeArtista();

    String getTitulo();

    Short getAnoLancamento();

    String getCapaUrl();

    Long getTotalMusicas();

    Long getDuracaoTotalSegundos();
}
