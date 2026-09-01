package gerenciador_musica_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_review")
    private Long idReview;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_musica")
    private Musica musica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album")
    private Album album;

    @Column(name = "nota", nullable = false, precision = 2, scale = 1)
    private BigDecimal nota;

    @Column(name = "texto")
    private String texto;

    @CreationTimestamp
    @Column(name = "criada_em", nullable = false, updatable = false)
    private OffsetDateTime criadaEm;

    @UpdateTimestamp
    @Column(name = "atualizada_em", nullable = false)
    private OffsetDateTime atualizadaEm;

    protected Review() {
    }

    public Review(
            Usuario usuario,
            Musica musica,
            Album album,
            BigDecimal nota,
            String texto
    ) {
        this.usuario = usuario;
        this.musica = musica;
        this.album = album;
        this.nota = nota;
        this.texto = texto;
    }

    public Long getIdReview() {
        return idReview;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Musica getMusica() {
        return musica;
    }

    public Album getAlbum() {
        return album;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public OffsetDateTime getCriadaEm() {
        return criadaEm;
    }

    public OffsetDateTime getAtualizadaEm() {
        return atualizadaEm;
    }
}
