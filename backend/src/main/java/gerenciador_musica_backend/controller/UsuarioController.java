package gerenciador_musica_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gerenciador_musica_backend.dto.UsuarioRequestDTO;
import gerenciador_musica_backend.dto.UsuarioResponseDTO;
import gerenciador_musica_backend.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.cadastrarUsuario(request);
        return ResponseEntity.ok(response);
    }
}
