package gerenciador_musica_backend.model;

import java.io.Serializable;
import java.util.Objects;

public class SeguidorUsuarioId implements Serializable {

    private Long seguidor;
    private Long seguido;

    public SeguidorUsuarioId() {
    }

    public SeguidorUsuarioId(Long seguidor, Long seguido) {
        this.seguidor = seguidor;
        this.seguido = seguido;
    }

    public Long getSeguidor() {
        return seguidor;
    }

    public void setSeguidor(Long seguidor) {
        this.seguidor = seguidor;
    }

    public Long getSeguido() {
        return seguido;
    }

    public void setSeguido(Long seguido) {
        this.seguido = seguido;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof SeguidorUsuarioId outro)) {
            return false;
        }

        return Objects.equals(seguidor, outro.seguidor)
                && Objects.equals(seguido, outro.seguido);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seguidor, seguido);
    }
}