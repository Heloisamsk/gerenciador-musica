package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.LoginRequestDTO;
import gerenciador_musica_backend.dto.LoginResponseDTO;
import gerenciador_musica_backend.dto.LogoutResponseDTO;
import gerenciador_musica_backend.exception.CredenciaisInvalidasException;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Teste de UNIDADE: o AuthService é testado sozinho.
 * UsuarioRepository, PasswordEncoder e JwtService são "dublês"
 * (mocks) criados pelo Mockito, então nenhum banco de dados real
 * e nenhuma geração de JWT de verdade acontecem aqui.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginRequestDTO request;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(
                "Maria",
                "maria@email.com",
                "senha-criptografada",
                Role.USER
        );

        request = new LoginRequestDTO();
        request.setEmail("maria@email.com");
        request.setSenha("senha123");
    }

    @Test
    void deveRetornarTokenQuandoCredenciaisEstaoCorretas() {
        when(usuarioRepository.findByEmail("maria@email.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "senha-criptografada"))
                .thenReturn(true);
        when(jwtService.gerarToken(usuario))
                .thenReturn("token-fake-123");

        LoginResponseDTO response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("token-fake-123");
        assertThat(response.getNome()).isEqualTo("Maria");
        assertThat(response.getEmail()).isEqualTo("maria@email.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoExiste() {
        when(usuarioRepository.findByEmail("maria@email.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha inválidos.");
    }

    @Test
    void deveLancarExcecaoQuandoSenhaEstaErrada() {
        when(usuarioRepository.findByEmail("maria@email.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "senha-criptografada"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha inválidos.");

        // Como a senha está errada, nunca deveria tentar gerar um token.
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    void deveMontarMensagemDeLogoutComEmailInformado() {
        LogoutResponseDTO response = authService.logout("maria@email.com");

        assertThat(response.getMensagem())
                .isEqualTo("Logout realizado com sucesso para maria@email.com.");
    }
}