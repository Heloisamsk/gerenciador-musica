package gerenciador_musica_backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(
            name = "nome",
            nullable = false,
            length = 255
    )
    private String nome;

    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 255
    )
    private String email;

    @Column(
            name = "senha",
            nullable = false,
            length = 255
    )
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 255
    )
    private Role role;

    @OneToMany(
            mappedBy = "usuario",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Playlist> playlists = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(
            String nome,
            String email,
            String senha,
            Role role
    ) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.role = role;
    }

    public void adicionarPlaylist(Playlist playlist) {
        playlists.add(playlist);
        playlist.setUsuario(this);
    }

    public void removerPlaylist(Playlist playlist) {
        playlists.remove(playlist);
        playlist.setUsuario(null);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }
}