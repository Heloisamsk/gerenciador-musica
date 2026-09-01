package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.service.SeguidorArtistaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeguidorArtistaController {

    private final SeguidorArtistaService seguidorArtistaService;

    public SeguidorArtistaController(
            SeguidorArtistaService seguidorArtistaService
    ) {
        this.seguidorArtistaService = seguidorArtistaService;
    }

    @PostMapping("/api/artistas/{id}/seguidor")
    public ResponseEntity<Void> seguirArtista(@PathVariable Long id) {
        seguidorArtistaService.seguirArtista(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/artistas/{id}/seguidor")
    public ResponseEntity<Void> deixarDeSeguirArtista(
            @PathVariable Long id
    ) {
        seguidorArtistaService.deixarDeSeguirArtista(id);
        return ResponseEntity.noContent().build();
    }
}
