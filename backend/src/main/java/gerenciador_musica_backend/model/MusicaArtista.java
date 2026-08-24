package gerenciador_musica_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "musica_artista")
@IdClass(MusicaArtistaId.class)
public class MusicaArtista {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_musica", nullable = false)
    private Musica musica;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_artista", nullable = false)
    private Artista artista;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel_participacao", nullable = false, length = 20)
    private PapelArtistaMusica papel;

    protected MusicaArtista() {
    }

    public MusicaArtista(
            Musica musica,
            Artista artista,
            PapelArtistaMusica papel
    ) {
        this.musica = musica;
        this.artista = artista;
        this.papel = papel;
    }

    public Musica getMusica() {
        return musica;
    }

    public Artista getArtista() {
        return artista;
    }

    public PapelArtistaMusica getPapel() {
        return papel;
    }

    void setPapel(PapelArtistaMusica papel) {
        this.papel = papel;
    }
}
