package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.PlaylistRequestDTO;
import gerenciador_musica_backend.dto.PlaylistResponseDTO;
import gerenciador_musica_backend.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping
    public ResponseEntity<PlaylistResponseDTO> criarPlaylist(@Valid @RequestBody PlaylistRequestDTO dto) {
        PlaylistResponseDTO playlistCriada = playlistService.criarPlaylist(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistCriada);
    }

    @GetMapping
    public ResponseEntity<List<PlaylistResponseDTO>> listarMinhas() {
        return ResponseEntity.ok(playlistService.listarMinhas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(playlistService.buscarPorId(id));
    }
}
