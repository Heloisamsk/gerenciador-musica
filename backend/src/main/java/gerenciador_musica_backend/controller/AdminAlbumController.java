package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.service.AlbumService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/albuns")
public class AdminAlbumController {

    private final AlbumService albumService;

    public AdminAlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping
    public ResponseEntity<AlbumResponseDTO> cadastrar(
            @Valid @RequestBody AlbumRequestDTO request
    ) {
        AlbumResponseDTO response =
                albumService.cadastrarAlbum(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}