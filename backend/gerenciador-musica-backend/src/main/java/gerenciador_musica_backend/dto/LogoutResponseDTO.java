package gerenciador_musica_backend.dto;

//JSON que será enviado para quem chamar a API. quando: POST /api/auth/logout.
//o Spring transformará automaticamente esse objeto em
//"mensagem":"Logout realizado com sucesso"

public class LogoutResponseDTO {
    private String mensagem;

    public LogoutResponseDTO() {
    }

    public LogoutResponseDTO(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
