package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.LogoutResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gerenciador_musica_backend.dto.LoginRequestDTO;
import gerenciador_musica_backend.dto.LoginResponseDTO;
import gerenciador_musica_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import gerenciador_musica_backend.model.Usuario;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid
            @RequestBody
            LoginRequestDTO request
    ) {
        LoginResponseDTO response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout(
            @AuthenticationPrincipal
            Usuario usuario
    ) {
        LogoutResponseDTO response =
                authService.logout(
                        usuario.getEmail()
                );

        return ResponseEntity.ok(response);
    }
}
