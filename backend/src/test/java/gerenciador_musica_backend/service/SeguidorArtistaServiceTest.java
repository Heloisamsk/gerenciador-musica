package gerenciador_musica_backend.service;

import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.SeguidorArtistaRepository;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeguidorArtistaServiceTest {

    @Mock
    private SeguidorArtistaRepository seguidorArtistaRepository;
    @Mock
    private ArtistaRepository artistaRepository;

    @InjectMocks
    private SeguidorArtistaService seguidorArtistaService;

    private Usuario usuarioLogado;
    private Artista artista;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(usuarioLogado, "id", 1L);

        artista = new Artista("Queen", "Queen", "Banda britânica.", null);
        ReflectionTestUtils.setField(artista, "idArtista", 7L);

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
    void deveSeguirArtista() {
        when(seguidorArtistaRepository.existsByUsuario_IdAndArtista_IdArtista(1L, 7L))
                .thenReturn(false);
        when(artistaRepository.findById(7L)).thenReturn(Optional.of(artista));

        seguidorArtistaService.seguirArtista(7L);

        verify(seguidorArtistaRepository).save(any());
    }

    @Test
    void naoDeveFazerNadaAoSeguirArtistaJaSeguido() {
        when(seguidorArtistaRepository.existsByUsuario_IdAndArtista_IdArtista(1L, 7L))
                .thenReturn(true);

        seguidorArtistaService.seguirArtista(7L);

        verify(seguidorArtistaRepository, never()).save(any());
        verify(artistaRepository, never()).findById(any());
    }

    @Test
    void deveLancarExcecaoAoSeguirArtistaInexistente() {
        when(seguidorArtistaRepository.existsByUsuario_IdAndArtista_IdArtista(1L, 7L))
                .thenReturn(false);
        when(artistaRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seguidorArtistaService.seguirArtista(7L))
                .isInstanceOf(ArtistaNaoEncontradoException.class);
    }

    @Test
    void deveDeixarDeSeguirArtista() {
        seguidorArtistaService.deixarDeSeguirArtista(7L);

        verify(seguidorArtistaRepository)
                .deleteByUsuario_IdAndArtista_IdArtista(1L, 7L);
    }

    @Test
    void deveLancarExcecaoQuandoNaoHaUsuarioAutenticado() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> seguidorArtistaService.seguirArtista(7L))
                .isInstanceOf(IllegalStateException.class);
    }
}
