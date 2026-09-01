package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.AlbumDetalheDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albuns")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public ResponseEntity<List<AlbumResponseDTO>> listarAlbuns(
            @RequestParam(
                    name = "artistaId",
                    required = false
            ) Long artistaId
    ) {
        List<AlbumResponseDTO> albuns = artistaId == null
                ? albumService.listarAlbuns()
                : albumService.listarAlbunsPorArtista(artistaId);

        return ResponseEntity.ok(albuns);
    }

    @GetMapping("/curtidos")
    public ResponseEntity<List<AlbumResponseDTO>> listarAlbunsCurtidos() {
        return ResponseEntity.ok(
                albumService.listarAlbunsCurtidos()
        );
    }

    @GetMapping("/{idAlbum}")
    public ResponseEntity<AlbumResponseDTO> buscarPorId(
            @PathVariable("idAlbum") Long idAlbum
    ) {
        return ResponseEntity.ok(
                albumService.buscarPorId(idAlbum)
        );
    }

    @GetMapping("/{idAlbum}/detalhes")
    public ResponseEntity<AlbumDetalheDTO> buscarDetalhesCatalogo(
            @PathVariable("idAlbum") Long idAlbum
    ) {
        return ResponseEntity.ok(
                albumService.buscarDetalhesCatalogo(idAlbum)
        );
    }
}
