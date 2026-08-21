package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.exception.AlbumDuplicadoException;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosAlbumInvalidosException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.repository.AlbumRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistaService artistaService;

    @InjectMocks
    private AlbumService albumService;

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
}
