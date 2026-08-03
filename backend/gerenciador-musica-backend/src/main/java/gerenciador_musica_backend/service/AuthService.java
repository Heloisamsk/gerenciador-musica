package gerenciador_musica_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import gerenciador_musica_backend.dto.LoginRequestDTO;
import gerenciador_musica_backend.dto.LoginResponseDTO;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.UsuarioRepository;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO request) {

        // Procura o usuário pelo e-mail
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("E-mail ou senha inválidos."));

        // Verifica se a senha está correta
        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("E-mail ou senha inválidos.");
        }

        // Gera o token JWT
        String token = jwtService.gerarToken(usuario);

        // Monta a resposta
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setRole(usuario.getRole());

        return response;
    }
}