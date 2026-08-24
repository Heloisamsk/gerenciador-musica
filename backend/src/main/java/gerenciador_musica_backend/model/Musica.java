package gerenciador_musica_backend.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "musica")
public class Musica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_musica")
    private Long idMusica;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_album", nullable = true)
    private Album album;

    @OneToMany(
            mappedBy = "musica",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @BatchSize(size = 50)
    private Set<MusicaArtista> creditosArtistas =
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

    @Column(name = "youtube_video_id", length = 11)
    private String youtubeVideoId;

    protected Musica() {
    }

    public Musica(
            String titulo,
            String letra,
            Integer duracaoSegundos,
            Short anoLancamento,
            Artista artistaPrincipal,
            Album album
    ) {
        this.titulo = titulo;
        this.letra = letra;
        this.duracaoSegundos = duracaoSegundos;
        this.anoLancamento = anoLancamento;
        definirCreditosArtistas(artistaPrincipal, Set.of());
        this.album = album;
    }

    public Long getIdMusica() {
        return idMusica;
    }

    public void setIdMusica(Long idMusica) {
        this.idMusica = idMusica;
    }

    public Artista getArtistaPrincipal() {
        return creditosArtistas.stream()
                .filter(credito -> credito.getPapel()
                        == PapelArtistaMusica.PRINCIPAL)
                .map(MusicaArtista::getArtista)
                .findFirst()
                .orElse(null);
    }

    public void setArtistaPrincipal(Artista artistaPrincipal) {
        Set<Artista> participantes = getArtistasParticipantes();
        participantes.removeIf(artista -> mesmoArtista(
                artista,
                artistaPrincipal
        ));

        definirCreditosArtistas(artistaPrincipal, participantes);
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Set<Artista> getArtistasParticipantes() {
        return creditosArtistas.stream()
                .filter(credito -> credito.getPapel()
                        == PapelArtistaMusica.FEAT)
                .map(MusicaArtista::getArtista)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setArtistasParticipantes(
            Set<Artista> artistasParticipantes
    ) {
        definirCreditosArtistas(
                getArtistaPrincipal(),
                artistasParticipantes
        );
    }

    public void definirCreditosArtistas(
            Artista artistaPrincipal,
            Set<Artista> artistasParticipantes
    ) {
        Objects.requireNonNull(
                artistaPrincipal,
                "O artista principal é obrigatório."
        );

        Set<Artista> participantes = artistasParticipantes == null
                ? Set.of()
                : new LinkedHashSet<>(artistasParticipantes);

        participantes.forEach(participante -> Objects.requireNonNull(
                participante,
                "O artista participante não pode ser nulo."
        ));

        boolean principalEntreParticipantes = participantes.stream()
                .anyMatch(artista -> mesmoArtista(
                        artista,
                        artistaPrincipal
                ));

        if (principalEntreParticipantes) {
            throw new IllegalArgumentException(
                    "O artista principal não pode ser participante."
            );
        }

        atualizarCreditosExistentes(artistaPrincipal, participantes);
        adicionarCreditoAusente(
                artistaPrincipal,
                PapelArtistaMusica.PRINCIPAL
        );

        participantes.forEach(participante -> adicionarCreditoAusente(
                participante,
                PapelArtistaMusica.FEAT
        ));
    }

    public Set<MusicaArtista> getCreditosArtistas() {
        return Collections.unmodifiableSet(creditosArtistas);
    }

    private void atualizarCreditosExistentes(
            Artista artistaPrincipal,
            Set<Artista> participantes
    ) {
        creditosArtistas.removeIf(credito -> {
            if (mesmoArtista(credito.getArtista(), artistaPrincipal)) {
                credito.setPapel(PapelArtistaMusica.PRINCIPAL);
                return false;
            }

            boolean continuaParticipante = participantes.stream()
                    .anyMatch(artista -> mesmoArtista(
                            artista,
                            credito.getArtista()
                    ));

            if (continuaParticipante) {
                credito.setPapel(PapelArtistaMusica.FEAT);
                return false;
            }

            return credito.getPapel() != PapelArtistaMusica.PRODUTOR;
        });
    }

    private void adicionarCreditoAusente(
            Artista artista,
            PapelArtistaMusica papel
    ) {
        boolean creditoJaExiste = creditosArtistas.stream()
                .anyMatch(credito -> mesmoArtista(
                        credito.getArtista(),
                        artista
                ));

        if (!creditoJaExiste) {
            creditosArtistas.add(new MusicaArtista(this, artista, papel));
        }
    }

    private static boolean mesmoArtista(
            Artista primeiro,
            Artista segundo
    ) {
        if (primeiro == segundo) {
            return true;
        }

        if (primeiro == null || segundo == null) {
            return false;
        }

        return primeiro.getIdArtista() != null
                && Objects.equals(
                        primeiro.getIdArtista(),
                        segundo.getIdArtista()
                );
    }

    public Set<Genero> getGeneros() {
        return generos;
    }

    public void setGeneros(Set<Genero> generos) {
        Set<Genero> novosGeneros = generos == null
                ? Set.of()
                : new LinkedHashSet<>(generos);

        this.generos.clear();
        this.generos.addAll(novosGeneros);
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

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }
}
