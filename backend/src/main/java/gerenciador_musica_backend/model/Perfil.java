package gerenciador_musica_backend.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_usuario",
            nullable = false,
            unique = true
    )
    private Usuario usuario;

    @Column(name = "foto_url", length = 2048)
    private String fotoUrl;

    @Column(name = "banner_url", length = 2048)
    private String bannerUrl;

    @Column(name = "biografia", columnDefinition = "TEXT")
    private String biografia;

    @Column(name = "frase_destaque", length = 500)
    private String fraseDestaque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artista_destaque")
    private Artista artistaDestaque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_musica_destaque")
    private Musica musicaDestaque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album_destaque")
    private Album albumDestaque;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_destaque_principal", length = 20)
    private TipoDestaquePerfil tipoDestaquePrincipal;

    @ManyToMany
    @JoinTable(
            name = "perfil_artista_favorito",
            joinColumns = @JoinColumn(name = "id_perfil"),
            inverseJoinColumns = @JoinColumn(name = "id_artista")
    )
    @OrderColumn(name = "ordem")
    private List<Artista> artistasFavoritos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "perfil_album_favorito",
            joinColumns = @JoinColumn(name = "id_perfil"),
            inverseJoinColumns = @JoinColumn(name = "id_album")
    )
    @OrderColumn(name = "ordem")
    private List<Album> albunsFavoritos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "perfil_musica_favorita",
            joinColumns = @JoinColumn(name = "id_perfil"),
            inverseJoinColumns = @JoinColumn(name = "id_musica")
    )
    @OrderColumn(name = "ordem")
    private List<Musica> musicasFavoritas = new ArrayList<>();

    protected Perfil() {
    }

    public Perfil(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public String getFraseDestaque() {
        return fraseDestaque;
    }

    public void setFraseDestaque(String fraseDestaque) {
        this.fraseDestaque = fraseDestaque;
    }

    public Artista getArtistaDestaque() {
        return artistaDestaque;
    }

    public void setArtistaDestaque(Artista artistaDestaque) {
        this.artistaDestaque = artistaDestaque;
    }

    public Musica getMusicaDestaque() {
        return musicaDestaque;
    }

    public void setMusicaDestaque(Musica musicaDestaque) {
        this.musicaDestaque = musicaDestaque;
    }

    public Album getAlbumDestaque() {
        return albumDestaque;
    }

    public void setAlbumDestaque(Album albumDestaque) {
        this.albumDestaque = albumDestaque;
    }

    public TipoDestaquePerfil getTipoDestaquePrincipal() {
        return tipoDestaquePrincipal;
    }

    public void setTipoDestaquePrincipal(
            TipoDestaquePerfil tipoDestaquePrincipal
    ) {
        this.tipoDestaquePrincipal = tipoDestaquePrincipal;
    }

    public List<Artista> getArtistasFavoritos() {
        return artistasFavoritos;
    }

    public void setArtistasFavoritos(List<Artista> artistasFavoritos) {
        this.artistasFavoritos.clear();
        this.artistasFavoritos.addAll(artistasFavoritos);
    }

    public List<Album> getAlbunsFavoritos() {
        return albunsFavoritos;
    }

    public void setAlbunsFavoritos(List<Album> albunsFavoritos) {
        this.albunsFavoritos.clear();
        this.albunsFavoritos.addAll(albunsFavoritos);
    }

    public List<Musica> getMusicasFavoritas() {
        return musicasFavoritas;
    }

    public void setMusicasFavoritas(List<Musica> musicasFavoritas) {
        this.musicasFavoritas.clear();
        this.musicasFavoritas.addAll(musicasFavoritas);
    }
}
