package gerenciador_musica_backend.repository.projection;

public interface ArtistaCatalogoResumoProjection {

    Long getIdArtista();

    String getNome();

    String getNomeCompleto();

    String getDescricao();

    String getFotoPerfilUrl();

    Long getTotalAlbuns();

    Long getTotalMusicasPrincipais();

    Long getTotalParticipacoes();

    Long getDuracaoTotalSegundos();
}
