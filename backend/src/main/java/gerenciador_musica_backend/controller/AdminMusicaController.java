package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.MusicaRequestDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.service.MusicaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/musicas")
public class AdminMusicaController {

    private final MusicaService musicaService;

    public AdminMusicaController(MusicaService musicaService) {
        this.musicaService = musicaService;
    }

    @PostMapping
    public ResponseEntity<MusicaResponseDTO> cadastrar(
            @Valid @RequestBody MusicaRequestDTO request
    ) {
        MusicaResponseDTO response =
                musicaService.cadastrarMusica(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/musicas/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MusicaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MusicaRequestDTO request
    ) {
        MusicaResponseDTO response = musicaService.atualizarMusica(
                id,
                request
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        musicaService.excluirMusica(id);

        return ResponseEntity.noContent().build();
    }
}
