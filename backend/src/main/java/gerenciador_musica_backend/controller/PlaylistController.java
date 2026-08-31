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
    public ResponseEntity<PlaylistResponseDTO> criarPlaylist(
            @Valid @RequestBody PlaylistRequestDTO dto
    ) {
        PlaylistResponseDTO playlistCriada =
                playlistService.criarPlaylist(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(playlistCriada);
    }

    @GetMapping
    public ResponseEntity<List<PlaylistResponseDTO>> listarPlaylists() {
        List<PlaylistResponseDTO> playlists =
                playlistService.listarMinhasPlaylists();

        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> buscarPlaylist(
            @PathVariable("id") Long id
    ) {
        PlaylistResponseDTO playlist =
                playlistService.buscarPlaylist(id);

        return ResponseEntity.ok(playlist);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> atualizarPlaylist(
            @PathVariable("id") Long id,
            @Valid @RequestBody PlaylistRequestDTO dto
    ) {
        PlaylistResponseDTO playlistAtualizada =
                playlistService.atualizarPlaylist(id, dto);

        return ResponseEntity.ok(playlistAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPlaylist(
            @PathVariable("id") Long id
    ) {
        playlistService.excluirPlaylist(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{playlistId}/musicas/{musicaId}")
    public ResponseEntity<Void> adicionarMusica(
            @PathVariable("playlistId") Long playlistId,
            @PathVariable("musicaId") Long musicaId
    ) {
        playlistService.adicionarMusica(playlistId, musicaId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{playlistId}/musicas/{musicaId}")
    public ResponseEntity<Void> removerMusica(
            @PathVariable("playlistId") Long playlistId,
            @PathVariable("musicaId") Long musicaId
    ) {
        playlistService.removerMusica(playlistId, musicaId);

        return ResponseEntity.noContent().build();
    }
}