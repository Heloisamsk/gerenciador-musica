package gerenciador_musica_backend.repository.projection;

public interface MusicaCatalogoProjection {

    Long getIdMusica();

    String getTitulo();

    Integer getDuracaoSegundos();

    Short getAnoLancamento();

    Long getIdArtistaPrincipal();

    String getNomeArtistaPrincipal();

    Long getIdAlbum();

    String getTituloAlbum();

    String getCapaUrl();

    String getGeneros();

    String getPapelArtista();
}
