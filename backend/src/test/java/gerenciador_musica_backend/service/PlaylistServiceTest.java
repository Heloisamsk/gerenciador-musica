package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.PlaylistRequestDTO;
import gerenciador_musica_backend.dto.PlaylistResponseDTO;
import gerenciador_musica_backend.exception.MusicaDuplicadaException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.exception.PlaylistAcessoNegadoException;
import gerenciador_musica_backend.exception.PlaylistEspecialException;
import gerenciador_musica_backend.exception.PlaylistNaoEncontradaException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Playlist;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
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

/*
 * Teste de UNIDADE do PlaylistService. Como o service descobre o
 * usuário logado através do SecurityContextHolder (não por um
 * parâmetro), simulamos a autenticação "de verdade" no
 * SecurityContextHolder antes de cada teste, em vez de mockar isso.
 */
@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private MusicaRepository musicaRepository;

    @InjectMocks
    private PlaylistService playlistService;

    private Usuario usuarioLogado;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        // Usuario.id só é preenchido pelo Hibernate na vida real (sem
        // setter público); usamos reflection para simular um usuário
        // já persistido, com id, dentro do teste.
        ReflectionTestUtils.setField(usuarioLogado, "id", 1L);

        autenticarComo(usuarioLogado);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(usuario, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void deveCriarPlaylistParaOUsuarioAutenticado() {
        PlaylistRequestDTO dto = new PlaylistRequestDTO();
        dto.setNome("Favoritas");
        dto.setDescricao("Minhas músicas preferidas");

        when(playlistRepository.save(any(Playlist.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlaylistResponseDTO response = playlistService.criarPlaylist(dto);

        assertThat(response.getNome()).isEqualTo("Favoritas");
        assertThat(response.getDescricao()).isEqualTo("Minhas músicas preferidas");
        assertThat(response.getMusicas()).isEmpty();
    }

    @Test
    void deveListarSomenteAsPlaylistsDoUsuarioAutenticado() {
        Playlist playlist = new Playlist("Rock", null, usuarioLogado);

        when(playlistRepository.findByUsuarioIdOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of(playlist));

        List<PlaylistResponseDTO> resultado = playlistService.listarMinhasPlaylists();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getNome()).isEqualTo("Rock");
    }

    @Test
    void deveBuscarPlaylistDoProprioUsuario() {
        Playlist playlist = new Playlist("Rock", null, usuarioLogado);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlist));

        PlaylistResponseDTO resultado = playlistService.buscarPlaylist(10L);

        assertThat(resultado.getNome()).isEqualTo("Rock");
    }

    @Test
    void deveConverterMusicasDaPlaylistComCapaDoAlbum() {
        Playlist playlist = new Playlist("Rock", null, usuarioLogado);
        Artista artista = new Artista(
                "Queen", "Queen", "Banda britânica de rock.", null
        );
        Album album = new Album(
                artista, "A Night at the Opera", (short) 1975,
                "https://exemplo.com/capa.jpg"
        );
        Musica musica = new Musica(
                "Bohemian Rhapsody", null, 354, (short) 1975, artista, album
        );
        ReflectionTestUtils.setField(musica, "idMusica", 5L);
        playlist.adicionarMusica(musica);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlist));

        PlaylistResponseDTO resultado = playlistService.buscarPlaylist(10L);

        assertThat(resultado.getMusicas()).hasSize(1);
        assertThat(resultado.getMusicas().getFirst().getId()).isEqualTo(5L);
        assertThat(resultado.getMusicas().getFirst().getTitulo())
                .isEqualTo("Bohemian Rhapsody");
        assertThat(resultado.getMusicas().getFirst().getArtista())
                .isEqualTo("Queen");
        assertThat(resultado.getMusicas().getFirst().getCapaUrl())
                .isEqualTo("https://exemplo.com/capa.jpg");
    }

    @Test
    void deveLancarExcecaoQuandoPlaylistNaoExiste() {
        when(playlistRepository.buscarComMusicasPorId(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.buscarPlaylist(999L))
                .isInstanceOf(PlaylistNaoEncontradaException.class);
    }

    @Test
    void deveLancarExcecaoQuandoPlaylistPertenceAOutroUsuario() {
        Usuario dono = new Usuario("Bruno", "bruno@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(dono, "id", 2L);

        Playlist playlistDeOutraPessoa = new Playlist("Rock", null, dono);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlistDeOutraPessoa));

        assertThatThrownBy(() -> playlistService.buscarPlaylist(10L))
                .isInstanceOf(PlaylistAcessoNegadoException.class);
    }

    @Test
    void deveAdicionarMusicaNaPlaylist() {
        Playlist playlist = new Playlist("Rock", null, usuarioLogado);
        Artista artista = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );
        Musica musica = new Musica(
                "Bohemian Rhapsody", null, 354, (short) 1975,
                artista, null
        );

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlist));
        when(musicaRepository.findById(5L))
                .thenReturn(Optional.of(musica));

        playlistService.adicionarMusica(10L, 5L);

        assertThat(playlist.getMusicas()).hasSize(1);
        verify(playlistRepository).save(playlist);
    }

    @Test
    void deveLancarExcecaoAoAdicionarMusicaJaPresenteNaPlaylist() {
        Playlist playlist = new Playlist("Rock", null, usuarioLogado);
        Artista artista = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );
        Musica musica = new Musica(
                "Bohemian Rhapsody", null, 354, (short) 1975,
                artista, null
        );
        ReflectionTestUtils.setField(musica, "idMusica", 5L);
        playlist.adicionarMusica(musica);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlist));
        when(musicaRepository.findById(5L))
                .thenReturn(Optional.of(musica));

        assertThatThrownBy(() -> playlistService.adicionarMusica(10L, 5L))
                .isInstanceOf(MusicaDuplicadaException.class);

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void deveRemoverMusicaDaPlaylist() {
        Playlist playlist = new Playlist("Rock", null, usuarioLogado);
        Artista artista = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );
        Musica musica = new Musica(
                "Bohemian Rhapsody", null, 354, (short) 1975,
                artista, null
        );
        ReflectionTestUtils.setField(musica, "idMusica", 5L);
        playlist.adicionarMusica(musica);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlist));

        playlistService.removerMusica(10L, 5L);

        assertThat(playlist.getMusicas()).isEmpty();
        verify(playlistRepository).save(playlist);
    }

    @Test
    void deveLancarExcecaoAoRemoverMusicaQueNaoEstaNaPlaylist() {
        Playlist playlist = new Playlist("Rock", null, usuarioLogado);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlist));

        assertThatThrownBy(() -> playlistService.removerMusica(10L, 5L))
                .isInstanceOf(MusicaNaoEncontradaException.class);

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void deveAtualizarNomeDescricaoECapaDaPlaylist() {
        Playlist playlist = new Playlist("Rock", "Antiga descrição", usuarioLogado);

        PlaylistRequestDTO dto = new PlaylistRequestDTO();
        dto.setNome("Rock clássico");
        dto.setDescricao("Nova descrição");
        dto.setCapaUrl("https://exemplo.com/capa.jpg");

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlist));
        when(playlistRepository.save(playlist)).thenReturn(playlist);

        PlaylistResponseDTO resultado = playlistService.atualizarPlaylist(10L, dto);

        assertThat(resultado.getNome()).isEqualTo("Rock clássico");
        assertThat(resultado.getDescricao()).isEqualTo("Nova descrição");
        assertThat(resultado.getCapaUrl()).isEqualTo("https://exemplo.com/capa.jpg");
    }

    @Test
    void deveLancarExcecaoAoAtualizarPlaylistDeOutroUsuario() {
        Usuario dono = new Usuario("Bruno", "bruno@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(dono, "id", 2L);

        Playlist playlistDeOutraPessoa = new Playlist("Rock", null, dono);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlistDeOutraPessoa));

        PlaylistRequestDTO dto = new PlaylistRequestDTO();
        dto.setNome("Novo nome");

        assertThatThrownBy(() -> playlistService.atualizarPlaylist(10L, dto))
                .isInstanceOf(PlaylistAcessoNegadoException.class);

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoAtualizarPlaylistEspecial() {
        Playlist favoritos = new Playlist("Favoritos", null, usuarioLogado);
        favoritos.setEspecial(true);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(favoritos));

        PlaylistRequestDTO dto = new PlaylistRequestDTO();
        dto.setNome("Novo nome");

        assertThatThrownBy(() -> playlistService.atualizarPlaylist(10L, dto))
                .isInstanceOf(PlaylistEspecialException.class);

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoExcluirPlaylistEspecial() {
        Playlist favoritos = new Playlist("Favoritos", null, usuarioLogado);
        favoritos.setEspecial(true);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(favoritos));

        assertThatThrownBy(() -> playlistService.excluirPlaylist(10L))
                .isInstanceOf(PlaylistEspecialException.class);

        verify(playlistRepository, never()).delete(any());
    }

    @Test
    void deveLancarExcecaoAoAdicionarMusicaNaPlaylistEspecial() {
        Playlist favoritos = new Playlist("Favoritos", null, usuarioLogado);
        favoritos.setEspecial(true);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(favoritos));

        assertThatThrownBy(() -> playlistService.adicionarMusica(10L, 5L))
                .isInstanceOf(PlaylistEspecialException.class);

        verify(musicaRepository, never()).findById(any());
    }

    @Test
    void deveLancarExcecaoAoRemoverMusicaDaPlaylistEspecial() {
        Playlist favoritos = new Playlist("Favoritos", null, usuarioLogado);
        favoritos.setEspecial(true);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(favoritos));

        assertThatThrownBy(() -> playlistService.removerMusica(10L, 5L))
                .isInstanceOf(PlaylistEspecialException.class);

        verify(playlistRepository, never()).save(any());
    }

    @Test
    void deveExcluirPlaylistDoProprioUsuario() {
        Playlist playlist = new Playlist("Rock", null, usuarioLogado);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlist));

        playlistService.excluirPlaylist(10L);

        verify(playlistRepository).delete(playlist);
    }

    @Test
    void deveLancarExcecaoAoExcluirPlaylistDeOutroUsuario() {
        Usuario dono = new Usuario("Bruno", "bruno@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(dono, "id", 2L);

        Playlist playlistDeOutraPessoa = new Playlist("Rock", null, dono);

        when(playlistRepository.buscarComMusicasPorId(10L))
                .thenReturn(Optional.of(playlistDeOutraPessoa));

        assertThatThrownBy(() -> playlistService.excluirPlaylist(10L))
                .isInstanceOf(PlaylistAcessoNegadoException.class);

        verify(playlistRepository, never()).delete(any());
    }
}
