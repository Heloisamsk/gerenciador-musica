package gerenciador_musica_backend.model;

import java.io.Serializable;
import java.util.Objects;

public class SeguidorArtistaId implements Serializable {

    private Long usuario;
    private Long artista;

    public SeguidorArtistaId() {
    }

    public SeguidorArtistaId(Long usuario, Long artista) {
        this.usuario = usuario;
        this.artista = artista;
    }

    public Long getUsuario() {
        return usuario;
    }

    public void setUsuario(Long usuario) {
        this.usuario = usuario;
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

        if (!(objeto instanceof SeguidorArtistaId outro)) {
            return false;
        }

        return Objects.equals(usuario, outro.usuario)
                && Objects.equals(artista, outro.artista);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, artista);
    }
}
