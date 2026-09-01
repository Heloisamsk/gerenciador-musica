package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.ArtistaDetalheDTO;
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

    @GetMapping("/seguidos")
    public ResponseEntity<List<ArtistaResponseDTO>> listarArtistasSeguidos() {
        return ResponseEntity.ok(
                artistaService.listarArtistasSeguidos()
        );
    }

    @GetMapping("/{idArtista}")
    public ResponseEntity<ArtistaResponseDTO> buscarPorId(
            @PathVariable("idArtista") Long idArtista
    ) {
        return ResponseEntity.ok(
                artistaService.buscarPorId(idArtista)
        );
    }

    @GetMapping("/{idArtista}/detalhes")
    public ResponseEntity<ArtistaDetalheDTO> buscarDetalhesCatalogo(
            @PathVariable("idArtista") Long idArtista
    ) {
        return ResponseEntity.ok(
                artistaService.buscarDetalhesCatalogo(idArtista)
        );
    }
}
