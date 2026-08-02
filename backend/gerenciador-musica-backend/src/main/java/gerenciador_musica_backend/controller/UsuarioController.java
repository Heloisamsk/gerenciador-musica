package gerenciador_musica_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gerenciador_musica_backend.dto.UsuarioRequestDTO;
import gerenciador_musica_backend.dto.UsuarioResponseDTO;
import gerenciador_musica_backend.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/cadastrar")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.cadastrarUsuario(request);
        return ResponseEntity.ok(response);
    }
}
