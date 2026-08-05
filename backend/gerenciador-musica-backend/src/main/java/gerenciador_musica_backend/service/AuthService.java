package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.LogoutResponseDTO;
import gerenciador_musica_backend.model.Role;
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

        // PARA TESTAR SEM O BANCO
        // Procura o usuário pelo e-mail
        //Usuario usuario = new Usuario();
        //usuario.setNome("Heloisa");
        //usuario.setEmail("heloisa@email.com");
        //usuario.setRole(Role.ADMIN);

        // senha = 123456 criptografada


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
    public LogoutResponseDTO logout() {

        LogoutResponseDTO response = new LogoutResponseDTO();

        response.setMensagem("Logout realizado com sucesso.");

        return response;
    }
}