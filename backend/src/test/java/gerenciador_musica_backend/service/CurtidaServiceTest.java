package gerenciador_musica_backend.service;

import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Playlist;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.CurtidaAlbumRepository;
import gerenciador_musica_backend.repository.CurtidaMusicaRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.PlaylistRepository;
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
class CurtidaServiceTest {

    @Mock
    private CurtidaMusicaRepository curtidaMusicaRepository;
    @Mock
    private CurtidaAlbumRepository curtidaAlbumRepository;
    @Mock
    private PlaylistRepository playlistRepository;
    @Mock
    private MusicaRepository musicaRepository;
    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private CurtidaService curtidaService;

    private Usuario usuarioLogado;
    private Musica musica;
    private Album album;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(usuarioLogado, "id", 1L);

        Artista artista = new Artista("Queen", "Queen", "Banda britânica.", null);
        musica = new Musica("Bohemian Rhapsody", null, 354, (short) 1975, artista, null);
        ReflectionTestUtils.setField(musica, "idMusica", 10L);

        album = new Album(artista, "A Night at the Opera", (short) 1975, null);
        ReflectionTestUtils.setField(album, "idAlbum", 20L);

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
    void deveCurtirMusicaECriarPlaylistFavoritosQuandoNaoExiste() {
        when(curtidaMusicaRepository.existsByUsuario_IdAndMusica_IdMusica(1L, 10L))
                .thenReturn(false);
        when(musicaRepository.findById(10L)).thenReturn(Optional.of(musica));
        when(playlistRepository.findByUsuarioIdAndEspecialTrue(1L))
                .thenReturn(Optional.empty());
        when(playlistRepository.save(any(Playlist.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        curtidaService.curtirMusica(10L);

        verify(curtidaMusicaRepository).save(any());
        verify(playlistRepository, org.mockito.Mockito.atLeastOnce()).save(any(Playlist.class));
    }

    @Test
    void deveCurtirMusicaEReaproveitarPlaylistFavoritosExistente() {
        Playlist favoritos = new Playlist("Favoritos", null, usuarioLogado);
        favoritos.setEspecial(true);

        when(curtidaMusicaRepository.existsByUsuario_IdAndMusica_IdMusica(1L, 10L))
                .thenReturn(false);
        when(musicaRepository.findById(10L)).thenReturn(Optional.of(musica));
        when(playlistRepository.findByUsuarioIdAndEspecialTrue(1L))
                .thenReturn(Optional.of(favoritos));

        curtidaService.curtirMusica(10L);

        assertThat(favoritos.getMusicas()).hasSize(1);
        verify(playlistRepository).save(favoritos);
    }

    @Test
    void naoDeveDuplicarMusicaNaPlaylistFavoritosSeJaEstiverLa() {
        Playlist favoritos = new Playlist("Favoritos", null, usuarioLogado);
        favoritos.setEspecial(true);
        favoritos.adicionarMusica(musica);

        when(curtidaMusicaRepository.existsByUsuario_IdAndMusica_IdMusica(1L, 10L))
                .thenReturn(false);
        when(musicaRepository.findById(10L)).thenReturn(Optional.of(musica));
        when(playlistRepository.findByUsuarioIdAndEspecialTrue(1L))
                .thenReturn(Optional.of(favoritos));

        curtidaService.curtirMusica(10L);

        assertThat(favoritos.getMusicas()).hasSize(1);
        verify(playlistRepository, never()).save(any());
    }

    @Test
    void naoDeveFazerNadaAoCurtirMusicaJaCurtida() {
        when(curtidaMusicaRepository.existsByUsuario_IdAndMusica_IdMusica(1L, 10L))
                .thenReturn(true);

        curtidaService.curtirMusica(10L);

        verify(curtidaMusicaRepository, never()).save(any());
        verify(musicaRepository, never()).findById(any());
    }

    @Test
    void deveLancarExcecaoAoCurtirMusicaInexistente() {
        when(curtidaMusicaRepository.existsByUsuario_IdAndMusica_IdMusica(1L, 10L))
                .thenReturn(false);
        when(musicaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> curtidaService.curtirMusica(10L))
                .isInstanceOf(MusicaNaoEncontradaException.class);
    }

    @Test
    void deveDescurtirMusicaERemoverDaPlaylistFavoritos() {
        Playlist favoritos = new Playlist("Favoritos", null, usuarioLogado);
        favoritos.setEspecial(true);
        favoritos.adicionarMusica(musica);

        when(playlistRepository.findByUsuarioIdAndEspecialTrue(1L))
                .thenReturn(Optional.of(favoritos));

        curtidaService.descurtirMusica(10L);

        verify(curtidaMusicaRepository)
                .deleteByUsuario_IdAndMusica_IdMusica(1L, 10L);
        assertThat(favoritos.getMusicas()).isEmpty();
        verify(playlistRepository).save(favoritos);
    }

    @Test
    void deveDescurtirMusicaSemPlaylistFavoritos() {
        when(playlistRepository.findByUsuarioIdAndEspecialTrue(1L))
                .thenReturn(Optional.empty());

        curtidaService.descurtirMusica(10L);

        verify(curtidaMusicaRepository)
                .deleteByUsuario_IdAndMusica_IdMusica(1L, 10L);
        verify(playlistRepository, never()).save(any());
    }

    @Test
    void deveCurtirAlbum() {
        when(curtidaAlbumRepository.existsByUsuario_IdAndAlbum_IdAlbum(1L, 20L))
                .thenReturn(false);
        when(albumRepository.findById(20L)).thenReturn(Optional.of(album));

        curtidaService.curtirAlbum(20L);

        verify(curtidaAlbumRepository).save(any());
    }

    @Test
    void naoDeveFazerNadaAoCurtirAlbumJaCurtido() {
        when(curtidaAlbumRepository.existsByUsuario_IdAndAlbum_IdAlbum(1L, 20L))
                .thenReturn(true);

        curtidaService.curtirAlbum(20L);

        verify(curtidaAlbumRepository, never()).save(any());
        verify(albumRepository, never()).findById(any());
    }

    @Test
    void deveLancarExcecaoAoCurtirAlbumInexistente() {
        when(curtidaAlbumRepository.existsByUsuario_IdAndAlbum_IdAlbum(1L, 20L))
                .thenReturn(false);
        when(albumRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> curtidaService.curtirAlbum(20L))
                .isInstanceOf(AlbumNaoEncontradoException.class);
    }

    @Test
    void deveDescurtirAlbum() {
        curtidaService.descurtirAlbum(20L);

        verify(curtidaAlbumRepository)
                .deleteByUsuario_IdAndAlbum_IdAlbum(1L, 20L);
    }

    @Test
    void deveLancarExcecaoQuandoNaoHaUsuarioAutenticado() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> curtidaService.curtirMusica(10L))
                .isInstanceOf(IllegalStateException.class);
    }
}
