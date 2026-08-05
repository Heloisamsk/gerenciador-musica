package gerenciador_musica_backend.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gerenciador_musica_backend.model.Usuario;

@RestController
@RequestMapping("/api")
public class AcessoController {

    @GetMapping("/user/perfil")
    public Map<String, String> perfil(
            @AuthenticationPrincipal Usuario usuario
    ) {
        return Map.of(
                "nome", usuario.getNome(),
                "email", usuario.getEmail(),
                "role", usuario.getRole().name()
        );
    }

    @GetMapping("/admin/teste")
    public Map<String, String> areaAdmin() {
        return Map.of(
                "message",
                "Acesso ADMIN autorizado."
        );
    }
}
