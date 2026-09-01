package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.UsuarioSeguidoResumoDTO;
import gerenciador_musica_backend.service.SeguidorUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Seguir/deixar de seguir outros usuários, para acompanhar suas
 * avaliações (reviews) no feed "Seguindo".
 */
@RestController
@RequestMapping("/api/usuarios")
public class SeguidorUsuarioController {

    private final SeguidorUsuarioService seguidorUsuarioService;

    public SeguidorUsuarioController(
            SeguidorUsuarioService seguidorUsuarioService
    ) {
        this.seguidorUsuarioService = seguidorUsuarioService;
    }

    @PostMapping("/{id}/seguidor")
    public ResponseEntity<Void> seguirUsuario(@PathVariable Long id) {
        seguidorUsuarioService.seguirUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/seguidor")
    public ResponseEntity<Void> deixarDeSeguirUsuario(
            @PathVariable Long id
    ) {
        seguidorUsuarioService.deixarDeSeguirUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/seguindo")
    public ResponseEntity<List<UsuarioSeguidoResumoDTO>> listarSeguindo(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                seguidorUsuarioService.listarSeguindo(id)
        );
    }

    @GetMapping("/{id}/seguidores")
    public ResponseEntity<List<UsuarioSeguidoResumoDTO>> listarSeguidores(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                seguidorUsuarioService.listarSeguidores(id)
        );
    }
}