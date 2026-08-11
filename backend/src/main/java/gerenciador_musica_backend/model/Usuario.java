package gerenciador_musica_backend.model;

import jakarta.persistence.*;

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

    // Um usuário pode ter Muitas playlists.
    @OneToMany(mappedBy = "usuario")
    private List<Playlist> playlists = new ArrayList<>();
    private Role role;

    public Usuario() {
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
}