package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.service.AlbumService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping
    public ResponseEntity<AlbumResponseDTO> cadastrarAlbum(
            @Valid @RequestBody AlbumRequestDTO request
    ) {
        return ResponseEntity.ok(
                albumService.cadastrarAlbum(request)
        );
    }
}
