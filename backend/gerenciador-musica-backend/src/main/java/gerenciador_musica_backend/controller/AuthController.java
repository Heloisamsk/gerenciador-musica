package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.LogoutResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gerenciador_musica_backend.dto.LoginRequestDTO;
import gerenciador_musica_backend.dto.LoginResponseDTO;
import gerenciador_musica_backend.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {

        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout() {

        LogoutResponseDTO response = authService.logout();

        return ResponseEntity.ok(response);

    }
}