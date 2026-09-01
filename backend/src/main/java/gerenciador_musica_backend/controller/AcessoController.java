package gerenciador_musica_backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gerenciador_musica_backend.dto.AtualizarPerfilRequestDTO;
import gerenciador_musica_backend.dto.PerfilResponseDTO;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.service.PerfilService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AcessoController {

    private final PerfilService perfilService;

    public AcessoController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/user/perfil")
    public PerfilResponseDTO perfil(
            @AuthenticationPrincipal Usuario usuario
    ) {
        return perfilService.obterPerfil(usuario);
    }

    @GetMapping("/usuarios/{idUsuario}/perfil")
    public PerfilResponseDTO perfilPublico(
            @PathVariable("idUsuario") Long idUsuario
    ) {
        return perfilService.obterPerfilPublico(idUsuario);
    }

    @PutMapping("/user/perfil")
    public PerfilResponseDTO atualizarPerfil(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AtualizarPerfilRequestDTO request
    ) {
        return perfilService.atualizarPerfil(usuario, request);
    }

    @GetMapping("/admin/teste")
    public java.util.Map<String, String> areaAdmin() {
        return java.util.Map.of("message", "Acesso ADMIN autorizado.");
    }
}
