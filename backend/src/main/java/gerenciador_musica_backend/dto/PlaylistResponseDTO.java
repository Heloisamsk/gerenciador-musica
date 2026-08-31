package gerenciador_musica_backend.dto;

import java.util.List;

public class PlaylistResponseDTO {

    // o que a API pode devolver: Não retornar senha, token ou entidade completa de usuário.
    private Long id;
    private String nome;
    private String descricao;
    private String capaUrl;
    private List<MusicaResumoDTO> musicas;

    public PlaylistResponseDTO() {
    }

    public PlaylistResponseDTO(
            Long id,
            String nome,
            String descricao,
            List<MusicaResumoDTO> musicas) {
        this(id, nome, descricao, null, musicas);
    }

    public PlaylistResponseDTO(
            Long id,
            String nome,
            String descricao,
            String capaUrl,
            List<MusicaResumoDTO> musicas) {

        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.capaUrl = capaUrl;
        this.musicas = musicas;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCapaUrl() {
        return capaUrl;
    }

    public List<MusicaResumoDTO> getMusicas() {
        return musicas;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setCapaUrl(String capaUrl) {
        this.capaUrl = capaUrl;
    }

    public void setMusicas(List<MusicaResumoDTO> musicas) {
        this.musicas = musicas;
    }
}