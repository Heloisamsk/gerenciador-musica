package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.UsuarioSeguidoResumoDTO;
import gerenciador_musica_backend.exception.SeguirUsuarioInvalidoException;
import gerenciador_musica_backend.exception.UsuarioNaoEncontradoException;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.SeguidorUsuarioRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeguidorUsuarioServiceTest {

    @Mock
    private SeguidorUsuarioRepository seguidorUsuarioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private SeguidorUsuarioService seguidorUsuarioService;

    private Usuario usuarioLogado;
    private Usuario alvo;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(usuarioLogado, "id", 1L);

        alvo = new Usuario("João", "joao@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(alvo, "id", 2L);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(usuarioLogado, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveSeguirUsuario() {
        when(seguidorUsuarioRepository.existsBySeguidor_IdAndSeguido_Id(1L, 2L))
                .thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(alvo));

        seguidorUsuarioService.seguirUsuario(2L);

        verify(seguidorUsuarioRepository).save(any());
    }

    @Test
    void naoDeveFazerNadaAoSeguirUsuarioJaSeguido() {
        when(seguidorUsuarioRepository.existsBySeguidor_IdAndSeguido_Id(1L, 2L))
                .thenReturn(true);

        seguidorUsuarioService.seguirUsuario(2L);

        verify(seguidorUsuarioRepository, never()).save(any());
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void deveLancarExcecaoAoTentarSeguirASiMesmo() {
        assertThatThrownBy(() -> seguidorUsuarioService.seguirUsuario(1L))
                .isInstanceOf(SeguirUsuarioInvalidoException.class)
                .hasMessageContaining("si mesmo");
    }

    @Test
    void deveLancarExcecaoAoSeguirComIdNulo() {
        assertThatThrownBy(() -> seguidorUsuarioService.seguirUsuario(null))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoAoSeguirUsuarioInexistente() {
        when(seguidorUsuarioRepository.existsBySeguidor_IdAndSeguido_Id(1L, 2L))
                .thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seguidorUsuarioService.seguirUsuario(2L))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    void deveDeixarDeSeguirUsuario() {
        seguidorUsuarioService.deixarDeSeguirUsuario(2L);

        verify(seguidorUsuarioRepository)
                .deleteBySeguidor_IdAndSeguido_Id(1L, 2L);
    }

    @Test
    void deveContarSeguidoresESeguindo() {
        when(seguidorUsuarioRepository.countBySeguido_Id(2L)).thenReturn(5L);
        when(seguidorUsuarioRepository.countBySeguidor_Id(2L)).thenReturn(3L);

        assertThat(seguidorUsuarioService.contarSeguidores(2L)).isEqualTo(5L);
        assertThat(seguidorUsuarioService.contarSeguindo(2L)).isEqualTo(3L);
    }

    @Test
    void deveListarQuemOUsuarioSegue() {
        when(seguidorUsuarioRepository.buscarUsuariosSeguidosPeloUsuario(1L))
                .thenReturn(List.of(alvo));

        List<UsuarioSeguidoResumoDTO> resultado =
                seguidorUsuarioService.listarSeguindo(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().idUsuario()).isEqualTo(2L);
        assertThat(resultado.getFirst().nome()).isEqualTo("João");
    }

    @Test
    void deveListarSeguidoresDoUsuario() {
        when(seguidorUsuarioRepository.buscarSeguidoresDoUsuario(2L))
                .thenReturn(List.of(usuarioLogado));

        List<UsuarioSeguidoResumoDTO> resultado =
                seguidorUsuarioService.listarSeguidores(2L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().idUsuario()).isEqualTo(1L);
    }

    @Test
    void deveLancarExcecaoQuandoNaoHaUsuarioAutenticado() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> seguidorUsuarioService.deixarDeSeguirUsuario(2L))
                .isInstanceOf(IllegalStateException.class);
    }
}
