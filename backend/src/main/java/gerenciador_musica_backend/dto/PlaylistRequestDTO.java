package gerenciador_musica_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class PlaylistRequestDTO {

    @NotBlank(message = "O nome da playlist é obrigatório")
    private String nome;

    private String descricao;

    private String capaUrl;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCapaUrl() {
        return capaUrl;
    }

    public void setCapaUrl(String capaUrl) {
        this.capaUrl = capaUrl;
    }
}
