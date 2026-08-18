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
import jakarta.persistence.UniqueConstraint;

// Restrição de unicidade: impede o cadastro de dois álbuns com o mesmo artista, título e ano de lançamento.
@Entity
@Table(
        name = "album",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_album_artista_titulo_ano",
                        columnNames = {
                                "id_artista",
                                "titulo",
                                "ano_lancamento"
                        }
                )
        }
)
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_album")
    private Long idAlbum;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_artista", nullable = false)
    private Artista artista;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "ano_lancamento", nullable = false)
    private Short anoLancamento;

    @Column(name = "capa_url", length = 2048)
    private String capaUrl;

    protected Album() {
    }

    public Album(
            Artista artista,
            String titulo,
            Short anoLancamento,
            String capaUrl
    ) {
        this.artista = artista;
        this.titulo = titulo;
        this.anoLancamento = anoLancamento;
        this.capaUrl = capaUrl;
    }

    public Long getIdAlbum() {
        return idAlbum;
    }

    public void setIdAlbum(Long idAlbum) {this.idAlbum = idAlbum;}

    public Artista getArtista() {
        return artista;
    }

    public void setArtista(Artista artista) {
        this.artista = artista;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Short getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(Short anoLancamento) {
        this.anoLancamento = anoLancamento;
    }

    public String getCapaUrl() {
        return capaUrl;
    }

    public void setCapaUrl(String capaUrl) {
        this.capaUrl = capaUrl;
    }
}