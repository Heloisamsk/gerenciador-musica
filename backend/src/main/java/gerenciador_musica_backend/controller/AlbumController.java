package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/albuns")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public ResponseEntity<List<AlbumResponseDTO>> listarAlbuns() {
        return ResponseEntity.ok(
                albumService.listarAlbuns()
        );
    }
}
