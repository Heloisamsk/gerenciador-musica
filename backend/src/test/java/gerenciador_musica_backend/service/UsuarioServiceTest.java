package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.UsuarioListagemDTO;
import gerenciador_musica_backend.dto.UsuarioRequestDTO;
import gerenciador_musica_backend.dto.UsuarioResponseDTO;
import gerenciador_musica_backend.exception.EmailJaCadastradoException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new UsuarioRequestDTO();
        request.setNome("Ana");
        request.setEmail("ana@email.com");
        request.setSenha("senha123");
    }

    @Test
    void deveCadastrarUsuarioCriptografandoASenha() {
        when(usuarioRepository.findByEmail("ana@email.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123"))
                .thenReturn("senha-criptografada");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponseDTO response = usuarioService.cadastrarUsuario(request);

        assertThat(response.getNome()).isEqualTo("Ana");
        assertThat(response.getEmail()).isEqualTo("ana@email.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);

        // Garante que a senha em texto puro nunca chegou a ser salva.
        verify(usuarioRepository).save(
                org.mockito.ArgumentMatchers.argThat(
                        usuario ->
                                usuario.getSenha()
                                        .equals("senha-criptografada")
                                        && usuario.getRole() == Role.USER
                )
        );
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaEstaCadastrado() {
        when(usuarioRepository.findByEmail("ana@email.com"))
                .thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> usuarioService.cadastrarUsuario(request))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveListarUsuariosCadastrados() {
        Usuario usuario1 = new Usuario("Ana", "ana@email.com", "hash1", Role.USER);
        Usuario usuario2 = new Usuario("Bruno", "bruno@email.com", "hash2", Role.ADMIN);

        when(usuarioRepository.findAll())
                .thenReturn(List.of(usuario1, usuario2));

        List<UsuarioListagemDTO> listagem = usuarioService.listarUsuarios();

        assertThat(listagem).hasSize(2);
        assertThat(listagem)
                .extracting(UsuarioListagemDTO::getEmail)
                .containsExactly("ana@email.com", "bruno@email.com");
    }
}