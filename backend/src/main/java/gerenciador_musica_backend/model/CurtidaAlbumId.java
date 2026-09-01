package gerenciador_musica_backend.model;

import java.io.Serializable;
import java.util.Objects;

public class CurtidaAlbumId implements Serializable {

    private Long usuario;
    private Long album;

    public CurtidaAlbumId() {
    }

    public CurtidaAlbumId(Long usuario, Long album) {
        this.usuario = usuario;
        this.album = album;
    }

    public Long getUsuario() {
        return usuario;
    }

    public void setUsuario(Long usuario) {
        this.usuario = usuario;
    }

    public Long getAlbum() {
        return album;
    }

    public void setAlbum(Long album) {
        this.album = album;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof CurtidaAlbumId outro)) {
            return false;
        }

        return Objects.equals(usuario, outro.usuario)
                && Objects.equals(album, outro.album);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, album);
    }
}
