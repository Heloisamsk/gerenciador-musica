package gerenciador_musica_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "musica")
public class Musica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_musica")
    private Long idMusica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_artista", nullable = false)
    private Artista artistaPrincipal;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_album", nullable = true)
    private Album album;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "musica_artista",
            joinColumns = @JoinColumn(
                    name = "id_musica",
                    nullable = false
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "id_artista",
                    nullable = false
            )
    )
    private Set<Artista> artistasParticipantes =
            new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "musica_genero",
            joinColumns = @JoinColumn(
                    name = "id_musica",
                    nullable = false
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "id_genero",
                    nullable = false
            )
    )
    private Set<Genero> generos =
            new LinkedHashSet<>();

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "letra", columnDefinition = "TEXT")
    private String letra;

    @Column(name = "duracao_segundos", nullable = false)
    private Integer duracaoSegundos;

    @Column(name = "ano_lancamento", nullable = false)
    private Short anoLancamento;

    protected Musica() {
    }

    public Musica(
            String titulo,
            String letra,
            Integer duracaoSegundos,
            Short anoLancamento,
            Artista artistaPrincipal,
            Album album,
            Set<Artista> participantes,
            Set<Genero> generos
    ) {
        this.titulo = titulo;
        this.letra = letra;
        this.duracaoSegundos = duracaoSegundos;
        this.anoLancamento = anoLancamento;
        this.artistaPrincipal = artistaPrincipal;
        this.album = album;

        this.artistasParticipantes =
                participantes == null
                        ? new LinkedHashSet<>()
                        : new LinkedHashSet<>(participantes);

        this.generos =
                generos == null
                        ? new LinkedHashSet<>()
                        : new LinkedHashSet<>(generos);
    }

    public Long getIdMusica() {
        return idMusica;
    }

    public void setIdMusica(Long idMusica) {
        this.idMusica = idMusica;
    }

    public Artista getArtistaPrincipal() {
        return artistaPrincipal;
    }

    public void setArtistaPrincipal(Artista artistaPrincipal) {
        this.artistaPrincipal = artistaPrincipal;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Set<Artista> getArtistasParticipantes() {
        return artistasParticipantes;
    }

    public void setArtistasParticipantes(
            Set<Artista> artistasParticipantes
    ) {
        this.artistasParticipantes =
                artistasParticipantes == null
                        ? new LinkedHashSet<>()
                        : new LinkedHashSet<>(artistasParticipantes);
    }

    public Set<Genero> getGeneros() {
        return generos;
    }

    public void setGeneros(Set<Genero> generos) {
        this.generos =
                generos == null
                        ? new LinkedHashSet<>()
                        : new LinkedHashSet<>(generos);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getLetra() {
        return letra;
    }

    public void setLetra(String letra) {
        this.letra = letra;
    }

    public Integer getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public void setDuracaoSegundos(Integer duracaoSegundos) {
        this.duracaoSegundos = duracaoSegundos;
    }

    public Short getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(Short anoLancamento) {
        this.anoLancamento = anoLancamento;
    }
}