package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.PlaylistRequestDTO;
import gerenciador_musica_backend.dto.PlaylistResponseDTO;
import gerenciador_musica_backend.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public ResponseEntity<List<PlaylistResponseDTO>> listarPlaylists() {

        List<PlaylistResponseDTO> playlists =
                playlistService.listarMinhasPlaylists();

        return ResponseEntity.ok(playlists);
    }
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> buscarPlaylist(
            @PathVariable Long id
    ) {

        PlaylistResponseDTO playlist =
                playlistService.buscarPlaylist(id);

        return ResponseEntity.ok(playlist);
    }
    
    @PostMapping
    public ResponseEntity<PlaylistResponseDTO> criarPlaylist(@Valid @RequestBody PlaylistRequestDTO dto) {
        
        PlaylistResponseDTO playlistCriada = playlistService.criarPlaylist(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistCriada);
    }
    @PostMapping("/{playlistId}/musicas/{musicaId}")
    public ResponseEntity<PlaylistResponseDTO> adicionarMusica(
            @PathVariable Long playlistId,
            @PathVariable Long musicaId
    ) {
        PlaylistResponseDTO playlistAtualizada =
                playlistService.adicionarMusica(playlistId, musicaId);

        return ResponseEntity.ok(playlistAtualizada);
    }
}
