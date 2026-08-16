package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.service.ArtistaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artistas")
public class ArtistaController {

    private final ArtistaService artistaService;

    public ArtistaController(ArtistaService artistaService) {
        this.artistaService = artistaService;
    }

    @GetMapping
    public ResponseEntity<List<ArtistaResponseDTO>> listarArtistas() {
        return ResponseEntity.ok(
                artistaService.listarArtistas()
        );
    }
}
