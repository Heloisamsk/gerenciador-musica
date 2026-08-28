package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.RelatorioAlbumDTO;
import gerenciador_musica_backend.dto.RelatorioArtistaDTO;
import gerenciador_musica_backend.dto.RelatorioCatalogoDTO;
import gerenciador_musica_backend.dto.TipoRelatorio;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RelatorioCsvService {

    private static final Pattern INICIO_FORMULA =
            Pattern.compile("^\\s*[=+@-]");
    private static final String QUEBRA_LINHA = "\r\n";

    private final RelatorioService relatorioService;

    public RelatorioCsvService(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    public byte[] exportar(TipoRelatorio tipo) {
        RelatorioCatalogoDTO relatorio =
                relatorioService.gerarRelatorioCatalogo();

        String csv = switch (tipo) {
            case ARTISTAS -> gerarCsvArtistas(relatorio);
            case ALBUNS -> gerarCsvAlbuns(relatorio);
        };

        return csv.getBytes(StandardCharsets.UTF_8);
    }

    private static String gerarCsvArtistas(
            RelatorioCatalogoDTO relatorio
    ) {
        StringBuilder csv = novoCsv();
        adicionarLinha(
                csv,
                "Artista",
                "Álbuns",
                "Músicas principais",
                "Participações",
                "Duração total (segundos)"
        );

        for (RelatorioArtistaDTO artista : relatorio.artistas()) {
            adicionarLinha(
                    csv,
                    artista.nome(),
                    Long.toString(artista.totalAlbuns()),
                    Long.toString(artista.totalMusicasPrincipais()),
                    Long.toString(artista.totalParticipacoes()),
                    Long.toString(artista.duracaoTotalSegundos())
            );
        }

        return csv.toString();
    }

    private static String gerarCsvAlbuns(
            RelatorioCatalogoDTO relatorio
    ) {
        StringBuilder csv = novoCsv();
        adicionarLinha(
                csv,
                "Álbum",
                "Artista",
                "Ano de lançamento",
                "Músicas",
                "Duração total (segundos)"
        );

        for (RelatorioAlbumDTO album : relatorio.albuns()) {
            adicionarLinha(
                    csv,
                    album.titulo(),
                    album.nomeArtista(),
                    Short.toString(album.anoLancamento()),
                    Long.toString(album.totalMusicas()),
                    Long.toString(album.duracaoTotalSegundos())
            );
        }

        return csv.toString();
    }

    private static StringBuilder novoCsv() {
        return new StringBuilder("\uFEFF");
    }

    private static void adicionarLinha(
            StringBuilder csv,
            String... campos
    ) {
        String linha = Arrays.stream(campos)
                .map(RelatorioCsvService::escaparCampo)
                .collect(Collectors.joining(";"));

        csv.append(linha).append(QUEBRA_LINHA);
    }

    private static String escaparCampo(String campo) {
        String campoSeguro = INICIO_FORMULA.matcher(campo).find()
                ? "'" + campo
                : campo;

        return '"' + campoSeguro.replace("\"", "\"\"") + '"';
    }
}
