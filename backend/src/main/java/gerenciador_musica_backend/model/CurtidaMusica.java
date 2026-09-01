package gerenciador_musica_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "curtida_musica")
@IdClass(CurtidaMusicaId.class)
public class CurtidaMusica {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_musica", nullable = false)
    private Musica musica;

    @CreationTimestamp
    @Column(
            name = "curtida_em",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime curtidaEm;

    protected CurtidaMusica() {
    }

    public CurtidaMusica(Usuario usuario, Musica musica) {
        this.usuario = usuario;
        this.musica = musica;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Musica getMusica() {
        return musica;
    }

    public OffsetDateTime getCurtidaEm() {
        return curtidaEm;
    }
}
