package gerenciador_musica_backend.dto;

public enum TipoRelatorio {
    ARTISTAS("relatorio-artistas.csv"),
    ALBUNS("relatorio-albuns.csv");

    private final String nomeArquivo;

    TipoRelatorio(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String nomeArquivo() {
        return nomeArquivo;
    }
}
