package gerenciador_musica_backend.dto;

public class MusicaResumoDTO {

    private Long id;
    private String titulo;
    private String artista;

    public MusicaResumoDTO() {
    }

    public MusicaResumoDTO(Long id, String titulo, String artista) {
        this.id = id;
        this.titulo = titulo;
        this.artista = artista;
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
}