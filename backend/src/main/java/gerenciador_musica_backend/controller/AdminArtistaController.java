package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.ArtistaRequestDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.service.ArtistaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/artistas")
public class AdminArtistaController {

    private final ArtistaService artistaService;

    public AdminArtistaController(ArtistaService artistaService) {
        this.artistaService = artistaService;
    }

    @PostMapping
    public ResponseEntity<ArtistaResponseDTO> cadastrar (
            @Valid @RequestBody ArtistaRequestDTO request
            ) {
        ArtistaResponseDTO response = artistaService.cadastrarArtista(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/artistas/{id}")
                .buildAndExpand(response.idArtista())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @DeleteMapping("/{idArtista}")
    public ResponseEntity<Void> excluir(
            @PathVariable("idArtista") Long idArtista
    ) {
        artistaService.excluirArtista(idArtista);

        return ResponseEntity.noContent().build();
    }
}
