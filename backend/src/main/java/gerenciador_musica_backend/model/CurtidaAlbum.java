package gerenciador_musica_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "curtida_album")
@IdClass(CurtidaAlbumId.class)
public class CurtidaAlbum {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_album", nullable = false)
    private Album album;

    @CreationTimestamp
    @Column(
            name = "curtida_em",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime curtidaEm;

    protected CurtidaAlbum() {
    }

    public CurtidaAlbum(Usuario usuario, Album album) {
        this.usuario = usuario;
        this.album = album;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Album getAlbum() {
        return album;
    }

    public OffsetDateTime getCurtidaEm() {
        return curtidaEm;
    }
}
