package gerenciador_musica_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario_segue_usuario")
@IdClass(SeguidorUsuarioId.class)
public class SeguidorUsuario {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_seguidor", nullable = false)
    private Usuario seguidor;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_seguido", nullable = false)
    private Usuario seguido;

    @CreationTimestamp
    @Column(
            name = "seguido_em",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime seguidoEm;

    protected SeguidorUsuario() {
    }

    public SeguidorUsuario(Usuario seguidor, Usuario seguido) {
        this.seguidor = seguidor;
        this.seguido = seguido;
    }

    public Usuario getSeguidor() {
        return seguidor;
    }

    public Usuario getSeguido() {
        return seguido;
    }

    public OffsetDateTime getSeguidoEm() {
        return seguidoEm;
    }
}