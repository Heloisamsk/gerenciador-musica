package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.service.MusicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/musicas")
public class MusicaController {

    private final MusicaService musicaService;

    public MusicaController(MusicaService musicaService) {
        this.musicaService = musicaService;
    }

    @GetMapping
    public ResponseEntity<List<MusicaResponseDTO>> listar() {
        return ResponseEntity.ok(
                musicaService.listarMusicas()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MusicaResponseDTO> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                musicaService.buscarPorId(id)
        );
    }
}