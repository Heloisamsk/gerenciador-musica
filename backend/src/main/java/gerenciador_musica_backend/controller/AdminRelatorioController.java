package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.RelatorioCatalogoDTO;
import gerenciador_musica_backend.dto.TipoRelatorio;
import gerenciador_musica_backend.service.RelatorioCsvService;
import gerenciador_musica_backend.service.RelatorioService;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/relatorios")
public class AdminRelatorioController {

    private static final MediaType TIPO_CSV =
            MediaType.parseMediaType("text/csv;charset=UTF-8");

    private final RelatorioService relatorioService;
    private final RelatorioCsvService relatorioCsvService;

    public AdminRelatorioController(
            RelatorioService relatorioService,
            RelatorioCsvService relatorioCsvService
    ) {
        this.relatorioService = relatorioService;
        this.relatorioCsvService = relatorioCsvService;
    }

    @GetMapping("/catalogo")
    public ResponseEntity<RelatorioCatalogoDTO> gerarCatalogo() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(relatorioService.gerarRelatorioCatalogo());
    }

    @GetMapping(value = "/catalogo.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportarCatalogo(
            @RequestParam(defaultValue = "ARTISTAS") TipoRelatorio tipo
    ) {
        ContentDisposition disposicao = ContentDisposition
                .attachment()
                .filename(tipo.nomeArquivo(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(TIPO_CSV)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposicao.toString()
                )
                .body(relatorioCsvService.exportar(tipo));
    }
}
