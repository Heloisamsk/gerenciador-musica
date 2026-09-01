package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AtualizarPerfilRequestDTO;
import gerenciador_musica_backend.dto.PerfilResponseDTO;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosPerfilInvalidosException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.exception.UsuarioNaoEncontradoException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Perfil;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.TipoDestaquePerfil;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.PerfilRepository;
import gerenciador_musica_backend.repository.ReviewRepository;
import gerenciador_musica_backend.repository.SeguidorUsuarioRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    private PerfilRepository perfilRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ArtistaRepository artistaRepository;
    @Mock
    private MusicaRepository musicaRepository;
    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private SeguidorUsuarioRepository seguidorUsuarioRepository;

    @InjectMocks
    private PerfilService perfilService;

    private Usuario usuario;
    private Perfil perfil;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(
                "Ana Liz",
                "ana@example.com",
                "hash",
                Role.USER
        );
        ReflectionTestUtils.setField(usuario, "id", 7L);
        perfil = new Perfil(usuario);

        lenient().when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        lenient().when(perfilRepository.findByUsuario_Id(7L))
                .thenReturn(Optional.of(perfil));
    }

    @Test
    void deveAtualizarImagensIdentidadeECuradoria() {
        Artista artistaDestaque = new Artista(
                "Marina Sena",
                "Marina de Oliveira Sena",
                "Cantora brasileira.",
                "https://exemplo.com/artista.jpg"
        );
        ReflectionTestUtils.setField(artistaDestaque, "idArtista", 5L);
        Artista artistaFavorita = new Artista(
                "Luedji Luna",
                "Luedji Gomes Santa Rita",
                "Cantora e compositora brasileira.",
                "https://exemplo.com/luedji.jpg"
        );
        ReflectionTestUtils.setField(artistaFavorita, "idArtista", 6L);
        when(artistaRepository.findById(5L))
                .thenReturn(Optional.of(artistaDestaque));
        when(artistaRepository.findById(6L))
                .thenReturn(Optional.of(artistaFavorita));

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "  Ana  ",
                "  analiz  ",
                "  https://exemplo.com/foto.jpg  ",
                "https://exemplo.com/banner.jpg",
                "  Pop brasileiro e música independente.  ",
                "Uma trilha para cada fase.",
                5L,
                null,
                null,
                TipoDestaquePerfil.ARTISTA,
                List.of(6L),
                List.of(),
                List.of()
        );

        PerfilResponseDTO resposta = perfilService.atualizarPerfil(
                usuario,
                request
        );

        assertThat(resposta.nome()).isEqualTo("Ana");
        assertThat(resposta.username()).isEqualTo("analiz");
        assertThat(resposta.bannerUrl())
                .isEqualTo("https://exemplo.com/banner.jpg");
        assertThat(resposta.tipoDestaquePrincipal())
                .isEqualTo(TipoDestaquePerfil.ARTISTA);
        assertThat(resposta.artistaDestaque().titulo())
                .isEqualTo("Marina Sena");
        assertThat(resposta.artistasFavoritos())
                .extracting(item -> item.titulo())
                .containsExactly("Luedji Luna");
        verify(perfilRepository).save(perfil);
    }

    @Test
    void deveRecusarDestaquePrincipalSemItemCorrespondente() {
        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                TipoDestaquePerfil.ALBUM,
                List.of(),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> perfilService.atualizarPerfil(usuario, request))
                .isInstanceOf(DadosPerfilInvalidosException.class)
                .hasMessageContaining("destaque principal");
    }

    @Test
    void deveRecusarDestaqueRepetidoNosFavoritos() {
        Artista artista = new Artista(
                "Marina Sena",
                "Marina de Oliveira Sena",
                "Cantora brasileira.",
                null
        );
        ReflectionTestUtils.setField(artista, "idArtista", 5L);
        when(artistaRepository.findById(5L)).thenReturn(Optional.of(artista));

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana",
                null,
                null,
                null,
                null,
                null,
                5L,
                null,
                null,
                TipoDestaquePerfil.ARTISTA,
                List.of(5L),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> perfilService.atualizarPerfil(usuario, request))
                .isInstanceOf(DadosPerfilInvalidosException.class)
                .hasMessageContaining("não pode ser repetido");
    }

    @Test
    void deveEscolherAutomaticamenteOPrimeiroFavorito() {
        Artista artista = new Artista(
                "Marina Sena",
                "Marina de Oliveira Sena",
                "Cantora brasileira.",
                null
        );
        ReflectionTestUtils.setField(artista, "idArtista", 5L);
        perfil.setArtistaDestaque(artista);

        PerfilResponseDTO resposta = perfilService.obterPerfil(usuario);

        assertThat(resposta.tipoDestaquePrincipal())
                .isEqualTo(TipoDestaquePerfil.ARTISTA);
    }

    @Test
    void deveIncluirEstatisticasDeReviewsNoPerfil() {
        when(reviewRepository.countByUsuario_IdAndMusicaIsNotNull(7L))
                .thenReturn(4L);
        when(reviewRepository.countByUsuario_IdAndAlbumIsNotNull(7L))
                .thenReturn(2L);

        PerfilResponseDTO resposta = perfilService.obterPerfil(usuario);

        assertThat(resposta.totalMusicasAvaliadas()).isEqualTo(4L);
        assertThat(resposta.totalAlbunsAvaliadas()).isEqualTo(2L);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioAutenticadoEhNulo() {
        assertThatThrownBy(() -> perfilService.obterPerfil(null))
                .isInstanceOf(DadosPerfilInvalidosException.class)
                .hasMessageContaining("identificar o usuário");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioAutenticadoNaoPossuiId() {
        Usuario semId = new Usuario(
                "Sem Id", "semid@example.com", "hash", Role.USER
        );

        assertThatThrownBy(() -> perfilService.obterPerfil(semId))
                .isInstanceOf(DadosPerfilInvalidosException.class)
                .hasMessageContaining("identificar o usuário");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioAutenticadoNaoExisteNoBanco() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.obterPerfil(usuario))
                .isInstanceOf(DadosPerfilInvalidosException.class)
                .hasMessageContaining("não foi encontrado");
    }

    @Test
    void deveCriarPerfilQuandoAindaNaoExiste() {
        when(perfilRepository.findByUsuario_Id(7L)).thenReturn(Optional.empty());
        when(perfilRepository.save(any(Perfil.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        PerfilResponseDTO resposta = perfilService.obterPerfil(usuario);

        assertThat(resposta.idUsuario()).isEqualTo(7L);
        verify(perfilRepository).save(any(Perfil.class));
    }

    @Test
    void deveRetornarPerfilPublicoIndicandoQueUsuarioAutenticadoSegue() {
        Usuario outro = new Usuario(
                "Marina", "marina@example.com", "hash", Role.USER
        );
        ReflectionTestUtils.setField(outro, "id", 9L);
        Perfil perfilOutro = new Perfil(outro);

        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(outro));
        when(perfilRepository.findByUsuario_Id(9L))
                .thenReturn(Optional.of(perfilOutro));
        when(seguidorUsuarioRepository
                .existsBySeguidor_IdAndSeguido_Id(7L, 9L))
                .thenReturn(true);

        PerfilResponseDTO resposta =
                perfilService.obterPerfilPublico(9L, usuario);

        assertThat(resposta.perfilDoUsuarioAutenticado()).isFalse();
        assertThat(resposta.seguindoPorUsuarioAutenticado()).isTrue();
    }

    @Test
    void deveLancarExcecaoAoBuscarPerfilPublicoDeUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                perfilService.obterPerfilPublico(99L, usuario))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoUsernameJaEstaEmUso() {
        when(usuarioRepository
                .existsByUsernameIgnoreCaseAndIdNot("novousername", 7L))
                .thenReturn(true);

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana", "novousername", null, null, null, null,
                null, null, null, null, List.of(), List.of(), List.of()
        );

        assertThatThrownBy(() -> perfilService.atualizarPerfil(usuario, request))
                .isInstanceOf(DadosPerfilInvalidosException.class)
                .hasMessageContaining("username já está sendo usado");
    }

    @Test
    void deveLancarExcecaoQuandoArtistaDestaqueNaoExiste() {
        when(artistaRepository.findById(5L)).thenReturn(Optional.empty());

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana", null, null, null, null, null,
                5L, null, null, null, List.of(), List.of(), List.of()
        );

        assertThatThrownBy(() -> perfilService.atualizarPerfil(usuario, request))
                .isInstanceOf(ArtistaNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoMusicaDestaqueNaoExiste() {
        when(musicaRepository.findById(20L)).thenReturn(Optional.empty());

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana", null, null, null, null, null,
                null, 20L, null, null, List.of(), List.of(), List.of()
        );

        assertThatThrownBy(() -> perfilService.atualizarPerfil(usuario, request))
                .isInstanceOf(MusicaNaoEncontradaException.class);
    }

    @Test
    void deveLancarExcecaoQuandoAlbumDestaqueNaoExiste() {
        when(albumRepository.findById(9L)).thenReturn(Optional.empty());

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana", null, null, null, null, null,
                null, null, 9L, null, List.of(), List.of(), List.of()
        );

        assertThatThrownBy(() -> perfilService.atualizarPerfil(usuario, request))
                .isInstanceOf(AlbumNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoQuantidadeDeFavoritosExcedeLimite() {
        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana", null, null, null, null, null,
                null, null, null, null,
                List.of(1L, 2L, 3L, 4L), List.of(), List.of()
        );

        assertThatThrownBy(() -> perfilService.atualizarPerfil(usuario, request))
                .isInstanceOf(DadosPerfilInvalidosException.class)
                .hasMessageContaining("no máximo três artistas favoritos");
    }

    @Test
    void deveLancarExcecaoQuandoFavoritosContemIdDuplicado() {
        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana", null, null, null, null, null,
                null, null, null, null,
                List.of(1L, 1L), List.of(), List.of()
        );

        assertThatThrownBy(() -> perfilService.atualizarPerfil(usuario, request))
                .isInstanceOf(DadosPerfilInvalidosException.class)
                .hasMessageContaining("seleção de artistas favoritos é inválida");
    }

    @Test
    void deveAtualizarPerfilComDestaqueDeMusicaUsandoCapaDoAlbum() {
        Artista artista = new Artista(
                "Queen", "Queen", "Banda britânica de rock.",
                "https://exemplo.com/queen.jpg"
        );
        ReflectionTestUtils.setField(artista, "idArtista", 5L);
        Album album = new Album(
                artista, "A Night at the Opera", (short) 1975,
                "https://exemplo.com/capa.jpg"
        );
        ReflectionTestUtils.setField(album, "idAlbum", 8L);
        Musica musica = new Musica(
                "Bohemian Rhapsody", null, 354, (short) 1975, artista, album
        );
        ReflectionTestUtils.setField(musica, "idMusica", 20L);
        when(musicaRepository.findById(20L)).thenReturn(Optional.of(musica));

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana", null, null, null, null, null,
                null, 20L, null, TipoDestaquePerfil.MUSICA,
                List.of(), List.of(), List.of()
        );

        PerfilResponseDTO resposta = perfilService.atualizarPerfil(usuario, request);

        assertThat(resposta.musicaDestaque().titulo())
                .isEqualTo("Bohemian Rhapsody");
        assertThat(resposta.musicaDestaque().subtitulo()).isEqualTo("Queen");
        assertThat(resposta.musicaDestaque().imagemUrl())
                .isEqualTo("https://exemplo.com/capa.jpg");
    }

    @Test
    void deveAtualizarPerfilComDestaqueDeMusicaSemAlbumUsandoFotoDoArtista() {
        Artista artista = new Artista(
                "Marina Sena", "Marina de Oliveira Sena",
                "Cantora brasileira.", "https://exemplo.com/artista.jpg"
        );
        ReflectionTestUtils.setField(artista, "idArtista", 5L);
        Musica musica = new Musica(
                "Faixa solo", null, 200, (short) 2020, artista, null
        );
        ReflectionTestUtils.setField(musica, "idMusica", 21L);
        when(musicaRepository.findById(21L)).thenReturn(Optional.of(musica));

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana", null, null, null, null, null,
                null, 21L, null, TipoDestaquePerfil.MUSICA,
                List.of(), List.of(), List.of()
        );

        PerfilResponseDTO resposta = perfilService.atualizarPerfil(usuario, request);

        assertThat(resposta.musicaDestaque().imagemUrl())
                .isEqualTo("https://exemplo.com/artista.jpg");
    }

    @Test
    void deveAtualizarPerfilComDestaqueDeAlbum() {
        Artista artista = new Artista(
                "Queen", "Queen", "Banda britânica de rock.", null
        );
        ReflectionTestUtils.setField(artista, "idArtista", 5L);
        Album album = new Album(
                artista, "News of the World", (short) 1977,
                "https://exemplo.com/now.jpg"
        );
        ReflectionTestUtils.setField(album, "idAlbum", 9L);
        when(albumRepository.findById(9L)).thenReturn(Optional.of(album));

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana", null, null, null, null, null,
                null, null, 9L, TipoDestaquePerfil.ALBUM,
                List.of(), List.of(), List.of()
        );

        PerfilResponseDTO resposta = perfilService.atualizarPerfil(usuario, request);

        assertThat(resposta.albumDestaque().titulo())
                .isEqualTo("News of the World");
        assertThat(resposta.albumDestaque().subtitulo()).isEqualTo("Queen");
        assertThat(resposta.albumDestaque().imagemUrl())
                .isEqualTo("https://exemplo.com/now.jpg");
    }
}
