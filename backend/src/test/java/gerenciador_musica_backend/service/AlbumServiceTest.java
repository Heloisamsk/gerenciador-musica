package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.AlbumDetalheDTO;
import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.exception.AlbumDuplicadoException;
import gerenciador_musica_backend.exception.AlbumEmUsoException;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosAlbumInvalidosException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.CurtidaAlbumRepository;
import gerenciador_musica_backend.repository.CurtidaMusicaRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.projection.AlbumCatalogoProjection;
import gerenciador_musica_backend.repository.projection.MusicaCatalogoProjection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Como converterParaResponse descobre o usuário logado através do
 * SecurityContextHolder (para enriquecer o resultado com "curtida"),
 * simulamos a autenticação antes de cada teste, igual ao PlaylistServiceTest.
 */
@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistaService artistaService;

    @Mock
    private MusicaRepository musicaRepository;

    @Mock
    private CurtidaAlbumRepository curtidaAlbumRepository;

    @Mock
    private CurtidaMusicaRepository curtidaMusicaRepository;

    @InjectMocks
    private AlbumService albumService;

    @BeforeEach
    void autenticar() {
        Usuario usuarioLogado =
                new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(usuarioLogado, "id", 1L);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuarioLogado, null, List.of()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        lenient()
                .when(curtidaAlbumRepository.existsByUsuario_IdAndAlbum_IdAlbum(
                        any(), any()
                ))
                .thenReturn(false);
        lenient()
                .when(curtidaAlbumRepository.buscarIdsCurtidosPeloUsuario(
                        any(), any()
                ))
                .thenReturn(java.util.Collections.emptySet());
        lenient()
                .when(curtidaMusicaRepository.buscarIdsCurtidosPeloUsuario(
                        any(), any()
                ))
                .thenReturn(java.util.Collections.emptySet());
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private Artista montarArtista(Long idArtista, String nome) {
        Artista artista = new Artista(
                nome,
                nome,
                "Descrição de " + nome,
                null
        );
        artista.setIdArtista(idArtista);
        return artista;
    }

    @Test
    void deveCadastrarAlbumNormalizandoOsDados() {
        Artista artista = montarArtista(1L, "Queen");
        AlbumRequestDTO request = new AlbumRequestDTO(
                "  A Night   at the Opera  ",
                1L,
                (short) 1975,
                "   "
        );

        when(artistaService.buscarEntidadePorId(1L))
                .thenReturn(artista);
        when(albumRepository
                .existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamento(
                        "A Night at the Opera",
                        1L,
                        (short) 1975
                ))
                .thenReturn(false);
        when(albumRepository.save(any(Album.class)))
                .thenAnswer(invocation -> {
                    Album album = invocation.getArgument(0);
                    album.setIdAlbum(10L);
                    return album;
                });

        AlbumResponseDTO response = albumService.cadastrarAlbum(request);

        assertThat(response.idAlbum()).isEqualTo(10L);
        assertThat(response.titulo()).isEqualTo("A Night at the Opera");
        assertThat(response.capaUrl()).isNull();
        assertThat(response.artista().id()).isEqualTo(1L);
        verify(albumRepository).save(any(Album.class));
    }

    @Test
    void deveLancarExcecaoQuandoRequestForNull() {
        assertThatThrownBy(() -> albumService.cadastrarAlbum(null))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage("Os dados do álbum são obrigatórios.");

        verify(albumRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoAlbumJaExistir() {
        Artista artista = montarArtista(1L, "Queen");
        AlbumRequestDTO request = new AlbumRequestDTO(
                "A Night at the Opera",
                1L,
                (short) 1975,
                null
        );

        when(artistaService.buscarEntidadePorId(1L))
                .thenReturn(artista);
        when(albumRepository
                .existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamento(
                        "A Night at the Opera",
                        1L,
                        (short) 1975
                ))
                .thenReturn(true);

        assertThatThrownBy(() -> albumService.cadastrarAlbum(request))
                .isInstanceOf(AlbumDuplicadoException.class);

        verify(albumRepository, never()).save(any());
    }

    @Test
    void deveListarSomenteAlbunsDoArtistaInformado() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                null
        );

        when(artistaService.buscarEntidadePorId(1L))
                .thenReturn(artista);
        when(albumRepository
                .findByArtistaIdArtistaOrderByTituloAscAnoLancamentoAsc(1L))
                .thenReturn(List.of(album));

        List<AlbumResponseDTO> resultado =
                albumService.listarAlbunsPorArtista(1L);

        assertThat(resultado)
                .extracting(AlbumResponseDTO::titulo)
                .containsExactly("A Night at the Opera");
        verify(artistaService).buscarEntidadePorId(1L);
    }

    @Test
    void deveBuscarAlbumQuandoPertencerAoArtista() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                null
        );

        when(albumRepository.findById(10L))
                .thenReturn(Optional.of(album));

        Album resultado = albumService.buscarAlbumDoArtista(10L, artista);

        assertThat(resultado).isSameAs(album);
    }

    @Test
    void devePermitirMusicaSemAlbum() {
        Artista artista = montarArtista(1L, "Queen");

        Album resultado = albumService.buscarAlbumDoArtista(null, artista);

        assertThat(resultado).isNull();
        verify(albumRepository, never()).findById(any());
    }

    @Test
    void deveRejeitarAlbumDeOutroArtista() {
        Artista artistaPrincipal = montarArtista(1L, "Queen");
        Artista outroArtista = montarArtista(2L, "David Bowie");
        Album album = new Album(
                outroArtista,
                "Heroes",
                (short) 1977,
                null
        );

        when(albumRepository.findById(20L))
                .thenReturn(Optional.of(album));

        assertThatThrownBy(
                () -> albumService.buscarAlbumDoArtista(
                        20L,
                        artistaPrincipal
                )
        )
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage(
                        "O álbum selecionado não pertence ao artista principal da música."
                );
    }

    @Test
    void deveLancarExcecaoQuandoAlbumNaoForEncontrado() {
        when(albumRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.buscarEntidadePorId(99L))
                .isInstanceOf(AlbumNaoEncontradoException.class);
    }

    @Test
    void deveListarTodosOsAlbunsOrdenados() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                null
        );
        album.setIdAlbum(10L);

        when(albumRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(album));

        List<AlbumResponseDTO> resultado = albumService.listarAlbuns();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().idAlbum()).isEqualTo(10L);
        assertThat(resultado.getFirst().titulo())
                .isEqualTo("A Night at the Opera");
        verify(albumRepository)
                .findAll(any(Sort.class));
    }

    @Test
    void deveBuscarAlbumPorIdConvertendoParaResponse() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                null
        );
        album.setIdAlbum(10L);

        when(albumRepository.findById(10L))
                .thenReturn(Optional.of(album));

        AlbumResponseDTO resultado = albumService.buscarPorId(10L);

        assertThat(resultado.idAlbum()).isEqualTo(10L);
        assertThat(resultado.artista().id()).isEqualTo(1L);
    }

    @Test
    void deveBuscarDetalhesDoAlbumPelasViews() {
        AlbumCatalogoProjection album = mock(
                AlbumCatalogoProjection.class
        );
        MusicaCatalogoProjection musica = mock(
                MusicaCatalogoProjection.class
        );
        MusicaCatalogoProjection musicaSemGenero = mock(
                MusicaCatalogoProjection.class
        );

        when(album.getIdAlbum()).thenReturn(10L);
        when(album.getIdArtista()).thenReturn(1L);
        when(album.getNomeArtista()).thenReturn("Queen");
        when(album.getTitulo()).thenReturn("A Night at the Opera");
        when(album.getAnoLancamento()).thenReturn((short) 1975);
        when(album.getCapaUrl()).thenReturn(null);
        when(album.getTotalMusicas()).thenReturn(2L);
        when(album.getDuracaoTotalSegundos()).thenReturn(573L);
        when(musica.getIdMusica()).thenReturn(20L);
        when(musica.getTitulo()).thenReturn("Bohemian Rhapsody");
        when(musica.getDuracaoSegundos()).thenReturn(354);
        when(musica.getGeneros())
                .thenReturn("Rock, Progressive Rock");
        when(musicaSemGenero.getIdMusica()).thenReturn(21L);
        when(musicaSemGenero.getTitulo()).thenReturn("Love of My Life");
        when(musicaSemGenero.getDuracaoSegundos()).thenReturn(219);
        when(musicaSemGenero.getGeneros()).thenReturn("  ");
        when(albumRepository.buscarCatalogoPorId(10L))
                .thenReturn(Optional.of(album));
        when(musicaRepository.buscarCatalogoPorAlbum(10L))
                .thenReturn(List.of(musica, musicaSemGenero));

        AlbumDetalheDTO resultado =
                albumService.buscarDetalhesCatalogo(10L);

        assertThat(resultado.album().titulo())
                .isEqualTo("A Night at the Opera");
        assertThat(resultado.generos())
                .containsExactly("Progressive Rock", "Rock");
        assertThat(resultado.musicas()).hasSize(2);
        assertThat(resultado.musicas().getFirst().generos())
                .containsExactly("Rock", "Progressive Rock");
        assertThat(resultado.musicas().getLast().generos()).isEmpty();
    }

    @Test
    void deveLancarExcecaoQuandoDetalhesDoAlbumNaoExistirem() {
        when(albumRepository.buscarCatalogoPorId(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> albumService.buscarDetalhesCatalogo(99L)
        )
                .isInstanceOf(AlbumNaoEncontradoException.class)
                .hasMessage("Álbum não encontrado com o ID: 99");

        verify(musicaRepository, never()).buscarCatalogoPorAlbum(any());
    }

    @Test
    void deveRejeitarIdInvalidoAoBuscarDetalhesDoAlbum() {
        assertThatThrownBy(
                () -> albumService.buscarDetalhesCatalogo(0L)
        )
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage("O ID do álbum deve ser válido.");

        verify(albumRepository, never()).buscarCatalogoPorId(any());
        verify(musicaRepository, never()).buscarCatalogoPorAlbum(any());
    }

    @Test
    void deveBuscarEntidadePorIdQuandoAlbumExistir() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                null
        );

        when(albumRepository.findById(10L))
                .thenReturn(Optional.of(album));

        Album resultado = albumService.buscarEntidadePorId(10L);

        assertThat(resultado).isSameAs(album);
    }

    @Test
    void deveRejeitarAlbumQuandoArtistaPrincipalForNull() {
        assertThatThrownBy(
                () -> albumService.buscarAlbumDoArtista(10L, null)
        )
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage(
                        "O artista principal da música é obrigatório."
                );

        verify(albumRepository, never()).findById(any());
    }

    @Test
    void deveRejeitarAlbumQuandoArtistaPrincipalNaoTiverId() {
        Artista artistaSemId = montarArtista(null, "Queen");

        assertThatThrownBy(
                () -> albumService.buscarAlbumDoArtista(
                        10L,
                        artistaSemId
                )
        )
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage(
                        "O artista principal da música é obrigatório."
                );

        verify(albumRepository, never()).findById(any());
    }

    @Test
    void deveRejeitarCadastroQuandoIdDoArtistaForNull() {
        AlbumRequestDTO request = new AlbumRequestDTO(
                "A Night at the Opera",
                null,
                (short) 1975,
                null
        );

        assertThatThrownBy(() -> albumService.cadastrarAlbum(request))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage("O ID do artista deve ser válido.");
    }

    @Test
    void deveRejeitarCadastroQuandoIdDoArtistaNaoForPositivo() {
        AlbumRequestDTO request = new AlbumRequestDTO(
                "A Night at the Opera",
                0L,
                (short) 1975,
                null
        );

        assertThatThrownBy(() -> albumService.cadastrarAlbum(request))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage("O ID do artista deve ser válido.");
    }

    @Test
    void deveRejeitarCadastroQuandoAnoForNull() {
        AlbumRequestDTO request = new AlbumRequestDTO(
                "A Night at the Opera",
                1L,
                null,
                null
        );

        assertThatThrownBy(() -> albumService.cadastrarAlbum(request))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage(
                        "O ano do álbum deve estar entre 1800 e 2100."
                );
    }

    @Test
    void deveRejeitarCadastroQuandoAnoForMenorQueOMinimo() {
        AlbumRequestDTO request = new AlbumRequestDTO(
                "A Night at the Opera",
                1L,
                (short) 1799,
                null
        );

        assertThatThrownBy(() -> albumService.cadastrarAlbum(request))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage(
                        "O ano do álbum deve estar entre 1800 e 2100."
                );
    }

    @Test
    void deveRejeitarCadastroQuandoAnoForMaiorQueOMaximo() {
        AlbumRequestDTO request = new AlbumRequestDTO(
                "A Night at the Opera",
                1L,
                (short) 2101,
                null
        );

        assertThatThrownBy(() -> albumService.cadastrarAlbum(request))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage(
                        "O ano do álbum deve estar entre 1800 e 2100."
                );
    }

    @Test
    void deveRejeitarCadastroQuandoTituloForNull() {
        AlbumRequestDTO request = new AlbumRequestDTO(
                null,
                1L,
                (short) 1975,
                null
        );

        assertThatThrownBy(() -> albumService.cadastrarAlbum(request))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage("O título do álbum é obrigatório.");
    }

    @Test
    void deveRejeitarCadastroQuandoTituloTiverApenasEspacos() {
        AlbumRequestDTO request = new AlbumRequestDTO(
                "   ",
                1L,
                (short) 1975,
                null
        );

        assertThatThrownBy(() -> albumService.cadastrarAlbum(request))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage("O título do álbum não pode ficar vazio.");
    }

    @Test
    void deveNormalizarUrlDeCapaInformada() {
        Artista artista = montarArtista(1L, "Queen");
        AlbumRequestDTO request = new AlbumRequestDTO(
                "A Night at the Opera",
                1L,
                (short) 1975,
                "  https://example.com/capa.jpg  "
        );

        when(artistaService.buscarEntidadePorId(1L))
                .thenReturn(artista);
        when(albumRepository
                .existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamento(
                        "A Night at the Opera",
                        1L,
                        (short) 1975
                ))
                .thenReturn(false);
        when(albumRepository.save(any(Album.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AlbumResponseDTO resultado = albumService.cadastrarAlbum(request);

        assertThat(resultado.capaUrl())
                .isEqualTo("https://example.com/capa.jpg");
    }

    @Test
    void deveAtualizarAlbumSemAlterarIdOuArtista() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "Título original",
                (short) 1974,
                "https://example.com/capa-original.jpg"
        );
        album.setIdAlbum(10L);
        AlbumAtualizacaoRequestDTO request =
                new AlbumAtualizacaoRequestDTO(
                        "  A Night   at the Opera  ",
                        (short) 1975,
                        "   "
                );

        when(albumRepository.findById(10L))
                .thenReturn(Optional.of(album));
        when(albumRepository
                .existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamentoAndIdAlbumNot(
                        "A Night at the Opera",
                        1L,
                        (short) 1975,
                        10L
                ))
                .thenReturn(false);

        AlbumResponseDTO resultado = albumService.atualizarAlbum(
                10L,
                request
        );

        assertThat(resultado.idAlbum()).isEqualTo(10L);
        assertThat(resultado.titulo())
                .isEqualTo("A Night at the Opera");
        assertThat(resultado.anoLancamento()).isEqualTo((short) 1975);
        assertThat(resultado.capaUrl()).isNull();
        assertThat(resultado.artista().id()).isEqualTo(1L);
        assertThat(album.getTitulo())
                .isEqualTo("A Night at the Opera");
        assertThat(album.getAnoLancamento()).isEqualTo((short) 1975);
        assertThat(album.getCapaUrl()).isNull();
        assertThat(album.getArtista()).isSameAs(artista);
        verify(albumRepository, never()).save(any());
        verify(artistaService, never()).buscarEntidadePorId(any());
    }

    @Test
    void deveAtualizarENormalizarUrlDaCapa() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                null
        );
        album.setIdAlbum(10L);
        AlbumAtualizacaoRequestDTO request =
                new AlbumAtualizacaoRequestDTO(
                        "A Night at the Opera",
                        (short) 1975,
                        "  https://example.com/nova-capa.jpg  "
                );

        when(albumRepository.findById(10L))
                .thenReturn(Optional.of(album));
        when(albumRepository
                .existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamentoAndIdAlbumNot(
                        "A Night at the Opera",
                        1L,
                        (short) 1975,
                        10L
                ))
                .thenReturn(false);

        AlbumResponseDTO resultado = albumService.atualizarAlbum(
                10L,
                request
        );

        assertThat(resultado.capaUrl())
                .isEqualTo("https://example.com/nova-capa.jpg");
        assertThat(album.getCapaUrl())
                .isEqualTo("https://example.com/nova-capa.jpg");
        assertThat(album.getArtista()).isSameAs(artista);
        verify(albumRepository, never()).save(any());
    }

    @Test
    void deveRejeitarAtualizacaoQuandoRequestForNull() {
        assertThatThrownBy(() -> albumService.atualizarAlbum(
                10L,
                null
        ))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage("Os dados do álbum são obrigatórios.");

        verify(albumRepository, never()).findById(any());
    }

    @Test
    void deveRejeitarAtualizacaoQuandoTituloForNull() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                null
        );
        album.setIdAlbum(10L);
        AlbumAtualizacaoRequestDTO request =
                new AlbumAtualizacaoRequestDTO(
                        null,
                        (short) 1975,
                        null
                );

        when(albumRepository.findById(10L))
                .thenReturn(Optional.of(album));

        assertThatThrownBy(() -> albumService.atualizarAlbum(
                10L,
                request
        ))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage("O título do álbum é obrigatório.");

        assertThat(album.getTitulo())
                .isEqualTo("A Night at the Opera");
    }

    @Test
    void deveRejeitarAtualizacaoQuandoAnoForNull() {
        AlbumAtualizacaoRequestDTO request =
                new AlbumAtualizacaoRequestDTO(
                        "A Night at the Opera",
                        null,
                        null
                );

        assertThatThrownBy(() -> albumService.atualizarAlbum(
                10L,
                request
        ))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage(
                        "O ano do álbum deve estar entre 1800 e 2100."
                );

        verify(albumRepository, never()).findById(any());
    }

    @Test
    void deveRejeitarAtualizacaoQuandoAnoForMenorQueOMinimo() {
        AlbumAtualizacaoRequestDTO request =
                new AlbumAtualizacaoRequestDTO(
                        "A Night at the Opera",
                        (short) 1799,
                        null
                );

        assertThatThrownBy(() -> albumService.atualizarAlbum(
                10L,
                request
        ))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage(
                        "O ano do álbum deve estar entre 1800 e 2100."
                );

        verify(albumRepository, never()).findById(any());
    }

    @Test
    void deveRejeitarAtualizacaoQuandoAnoForMaiorQueOMaximo() {
        AlbumAtualizacaoRequestDTO request =
                new AlbumAtualizacaoRequestDTO(
                        "A Night at the Opera",
                        (short) 2101,
                        null
                );

        assertThatThrownBy(() -> albumService.atualizarAlbum(
                10L,
                request
        ))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage(
                        "O ano do álbum deve estar entre 1800 e 2100."
                );

        verify(albumRepository, never()).findById(any());
    }

    @Test
    void devePermitirManterOsDadosAtuaisDoAlbum() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                null
        );
        album.setIdAlbum(10L);
        AlbumAtualizacaoRequestDTO request =
                new AlbumAtualizacaoRequestDTO(
                        "A Night at the Opera",
                        (short) 1975,
                        null
                );

        when(albumRepository.findById(10L))
                .thenReturn(Optional.of(album));
        when(albumRepository
                .existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamentoAndIdAlbumNot(
                        "A Night at the Opera",
                        1L,
                        (short) 1975,
                        10L
                ))
                .thenReturn(false);

        AlbumResponseDTO resultado = albumService.atualizarAlbum(
                10L,
                request
        );

        assertThat(resultado.idAlbum()).isEqualTo(10L);
        assertThat(resultado.titulo())
                .isEqualTo("A Night at the Opera");
    }

    @Test
    void deveRejeitarAtualizacaoDuplicadaComOutroAlbum() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "Sheer Heart Attack",
                (short) 1974,
                null
        );
        album.setIdAlbum(20L);
        AlbumAtualizacaoRequestDTO request =
                new AlbumAtualizacaoRequestDTO(
                        "A Night at the Opera",
                        (short) 1975,
                        null
                );

        when(albumRepository.findById(20L))
                .thenReturn(Optional.of(album));
        when(albumRepository
                .existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamentoAndIdAlbumNot(
                        "A Night at the Opera",
                        1L,
                        (short) 1975,
                        20L
                ))
                .thenReturn(true);

        assertThatThrownBy(() -> albumService.atualizarAlbum(
                20L,
                request
        ))
                .isInstanceOf(AlbumDuplicadoException.class)
                .hasMessageContaining("A Night at the Opera");

        assertThat(album.getTitulo()).isEqualTo("Sheer Heart Attack");
        assertThat(album.getAnoLancamento()).isEqualTo((short) 1974);
        verify(albumRepository, never()).save(any());
    }

    @Test
    void deveRejeitarAtualizacaoQuandoAlbumNaoExistir() {
        AlbumAtualizacaoRequestDTO request =
                new AlbumAtualizacaoRequestDTO(
                        "A Night at the Opera",
                        (short) 1975,
                        null
                );

        when(albumRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.atualizarAlbum(
                99L,
                request
        )).isInstanceOf(AlbumNaoEncontradoException.class);

        verify(albumRepository, never()).save(any());
    }

    @Test
    void deveRejeitarAtualizacaoQuandoIdNaoForPositivo() {
        AlbumAtualizacaoRequestDTO request =
                new AlbumAtualizacaoRequestDTO(
                        "A Night at the Opera",
                        (short) 1975,
                        null
                );

        assertThatThrownBy(() -> albumService.atualizarAlbum(
                0L,
                request
        ))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage("O ID do álbum deve ser válido.");

        verify(albumRepository, never()).findById(any());
    }

    @Test
    void deveExcluirAlbumSemMusicasAssociadas() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                null
        );
        album.setIdAlbum(10L);

        when(albumRepository.findById(10L))
                .thenReturn(Optional.of(album));
        when(musicaRepository.existsByAlbum_IdAlbum(10L))
                .thenReturn(false);

        albumService.excluirAlbum(10L);

        verify(albumRepository).delete(album);
        assertThat(album.getArtista()).isSameAs(artista);
    }

    @Test
    void deveBloquearExclusaoQuandoAlbumPossuirMusicas() {
        Artista artista = montarArtista(1L, "Queen");
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                null
        );
        album.setIdAlbum(10L);

        when(albumRepository.findById(10L))
                .thenReturn(Optional.of(album));
        when(musicaRepository.existsByAlbum_IdAlbum(10L))
                .thenReturn(true);

        assertThatThrownBy(() -> albumService.excluirAlbum(10L))
                .isInstanceOf(AlbumEmUsoException.class)
                .hasMessage(
                        "Não é possível excluir o álbum porque "
                                + "ele possui músicas associadas."
                );

        verify(albumRepository, never()).delete(any());
    }

    @Test
    void deveRejeitarExclusaoQuandoAlbumNaoExistir() {
        when(albumRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.excluirAlbum(99L))
                .isInstanceOf(AlbumNaoEncontradoException.class);

        verify(musicaRepository, never()).existsByAlbum_IdAlbum(any());
        verify(albumRepository, never()).delete(any());
    }

    @Test
    void deveRejeitarExclusaoQuandoIdNaoForPositivo() {
        assertThatThrownBy(() -> albumService.excluirAlbum(-1L))
                .isInstanceOf(DadosAlbumInvalidosException.class)
                .hasMessage("O ID do álbum deve ser válido.");

        verify(albumRepository, never()).findById(any());
        verify(musicaRepository, never()).existsByAlbum_IdAlbum(any());
    }
}
