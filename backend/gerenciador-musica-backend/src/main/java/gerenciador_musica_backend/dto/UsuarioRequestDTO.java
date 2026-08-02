package gerenciador_musica_backend.dto;

import gerenciador_musica_backend.model.Role;

public class UsuarioRequestDTO {

    private String nome;
    private String email;
    private String senha;
    private Role role;
    
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
