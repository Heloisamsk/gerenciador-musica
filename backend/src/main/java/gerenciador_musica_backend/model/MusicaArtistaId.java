package gerenciador_musica_backend.model;

import java.io.Serializable;
import java.io.Serial;
import java.util.Objects;

public class MusicaArtistaId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long musica;
    private Long artista;

    public MusicaArtistaId() {
    }

    public MusicaArtistaId(Long musica, Long artista) {
        this.musica = musica;
        this.artista = artista;
    }

    public Long getMusica() {
        return musica;
    }

    public void setMusica(Long musica) {
        this.musica = musica;
    }

    public Long getArtista() {
        return artista;
    }

    public void setArtista(Long artista) {
        this.artista = artista;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof MusicaArtistaId outro)) {
            return false;
        }

        return Objects.equals(musica, outro.musica)
                && Objects.equals(artista, outro.artista);
    }

    @Override
    public int hashCode() {
        return Objects.hash(musica, artista);
    }
}
