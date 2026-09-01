package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.BuscaResultadoDTO;
import gerenciador_musica_backend.service.BuscaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/busca")
public class BuscaController {

    private final BuscaService buscaService;

    public BuscaController(BuscaService buscaService) {
        this.buscaService = buscaService;
    }

    /**
     * GET /api/busca?q=termo — busca unificada por músicas, álbuns e
     * artistas cujo nome/título contenha o termo informado, limitada aos
     * 5 melhores resultados de cada tipo. Usada pela busca instantânea.
     */
    @GetMapping
    public ResponseEntity<BuscaResultadoDTO> buscar(
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(buscaService.buscar(q));
    }
}
