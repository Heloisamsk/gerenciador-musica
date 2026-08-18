package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.GeneroResumoDTO;
import gerenciador_musica_backend.service.GeneroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/generos")
public class GeneroController {

    private final GeneroService generoService;

    public GeneroController(GeneroService generoService) {
        this.generoService = generoService;
    }

    @GetMapping
    public ResponseEntity<List<GeneroResumoDTO>> listarGeneros() {
        return ResponseEntity.ok(
                generoService.listarGeneros()
        );
    }
}
