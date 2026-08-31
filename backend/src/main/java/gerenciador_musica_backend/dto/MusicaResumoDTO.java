package gerenciador_musica_backend.dto;

public class MusicaResumoDTO {

    private Long id;
    private String titulo;
    private String artista;
    private String capaUrl;

    public MusicaResumoDTO() {
    }

    public MusicaResumoDTO(Long id, String titulo, String artista, String capaUrl) {
        this.id = id;
        this.titulo = titulo;
        this.artista = artista;
        this.capaUrl = capaUrl;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getArtista() {
        return artista;
    }

    public String getCapaUrl() {
        return capaUrl;
    }
}