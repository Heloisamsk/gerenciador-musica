package gerenciador_musica_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario_segue_artista")
@IdClass(SeguidorArtistaId.class)
public class SeguidorArtista {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_artista", nullable = false)
    private Artista artista;

    @CreationTimestamp
    @Column(
            name = "seguido_em",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime seguidoEm;

    protected SeguidorArtista() {
    }

    public SeguidorArtista(Usuario usuario, Artista artista) {
        this.usuario = usuario;
        this.artista = artista;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Artista getArtista() {
        return artista;
    }

    public OffsetDateTime getSeguidoEm() {
        return seguidoEm;
    }
}
