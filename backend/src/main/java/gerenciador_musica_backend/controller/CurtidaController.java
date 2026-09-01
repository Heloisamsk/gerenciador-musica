package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.service.CurtidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurtidaController {

    private final CurtidaService curtidaService;

    public CurtidaController(CurtidaService curtidaService) {
        this.curtidaService = curtidaService;
    }

    @PostMapping("/api/musicas/{id}/curtida")
    public ResponseEntity<Void> curtirMusica(@PathVariable Long id) {
        curtidaService.curtirMusica(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/musicas/{id}/curtida")
    public ResponseEntity<Void> descurtirMusica(@PathVariable Long id) {
        curtidaService.descurtirMusica(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/albuns/{id}/curtida")
    public ResponseEntity<Void> curtirAlbum(@PathVariable Long id) {
        curtidaService.curtirAlbum(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/albuns/{id}/curtida")
    public ResponseEntity<Void> descurtirAlbum(@PathVariable Long id) {
        curtidaService.descurtirAlbum(id);
        return ResponseEntity.noContent().build();
    }
}
