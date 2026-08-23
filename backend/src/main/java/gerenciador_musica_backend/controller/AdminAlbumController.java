package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.AlbumAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.service.AlbumService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/albuns/{id}")
                .buildAndExpand(response.idAlbum())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PutMapping("/{idAlbum}")
    public ResponseEntity<AlbumResponseDTO> atualizar(
            @PathVariable("idAlbum") Long idAlbum,
            @Valid @RequestBody AlbumAtualizacaoRequestDTO request
    ) {
        return ResponseEntity.ok(
                albumService.atualizarAlbum(idAlbum, request)
        );
    }

    @DeleteMapping("/{idAlbum}")
    public ResponseEntity<Void> excluir(
            @PathVariable("idAlbum") Long idAlbum
    ) {
        albumService.excluirAlbum(idAlbum);

        return ResponseEntity.noContent().build();
    }
}
