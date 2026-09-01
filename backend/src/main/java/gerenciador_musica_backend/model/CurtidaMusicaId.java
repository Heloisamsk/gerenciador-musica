package gerenciador_musica_backend.model;

import java.io.Serializable;
import java.util.Objects;

public class CurtidaMusicaId implements Serializable {

    private Long usuario;
    private Long musica;

    public CurtidaMusicaId() {
    }

    public CurtidaMusicaId(Long usuario, Long musica) {
        this.usuario = usuario;
        this.musica = musica;
    }

    public Long getUsuario() {
        return usuario;
    }

    public void setUsuario(Long usuario) {
        this.usuario = usuario;
    }

    public Long getMusica() {
        return musica;
    }

    public void setMusica(Long musica) {
        this.musica = musica;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof CurtidaMusicaId outro)) {
            return false;
        }

        return Objects.equals(usuario, outro.usuario)
                && Objects.equals(musica, outro.musica);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, musica);
    }
}
