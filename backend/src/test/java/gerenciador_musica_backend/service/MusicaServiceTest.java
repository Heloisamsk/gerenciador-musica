package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.MusicaFiltroDTO;
import gerenciador_musica_backend.dto.MusicaListagemDTO;
import gerenciador_musica_backend.dto.MusicaRequestDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.dto.PaginaResponseDTO;
import gerenciador_musica_backend.exception.DadosMusicaInvalidosException;
import gerenciador_musica_backend.exception.MusicaDuplicadaException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Genero;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.GeneroRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Teste de UNIDADE do MusicaService: os 4 repositórios são mockados,
 * então nada disso toca um banco de dados de verdade.
 */
@ExtendWith(MockitoExtension.class)
class MusicaServiceTest {

    @Mock
    private MusicaRepository musicaRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistaService artistaService;

    @Mock
    private GeneroRepository generoRepository;

    @InjectMocks
    private MusicaService musicaService;

    private MusicaRequestDTO montarRequestValida() {
        return new MusicaRequestDTO(
                "Bohemian Rhapsody",
                null,
                354,
                (short) 1975,
                1L,
                Set.of(),
                new AlbumRequestDTO(
                        "A Night at the Opera",
                        (short) 1975,
                        null
                ),
                Set.of("Rock")
        );
    }

    @Test
    void deveCadastrarMusicaComSucessoUsandoArtistaExistenteECriandoAlbumEGenero() {
        MusicaRequestDTO request = montarRequestValida();

        Artista artistaExistente = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );
        Album albumSalvo = new Album(
                artistaExistente,
                "A Night at the Opera",
                (short) 1975,
                null
        );
        Genero generoSalvo = new Genero("Rock");

        when(artistaService.buscarEntidadePorId(1L))
                .thenReturn(artistaExistente);

        when(albumRepository.findByTituloIgnoreCaseAndArtistaAndAnoLancamento(
                "A Night at the Opera",
                artistaExistente,
                (short) 1975
        )).thenReturn(Optional.empty());

        when(albumRepository.save(any(Album.class)))
                .thenReturn(albumSalvo);

        when(musicaRepository.existsByAlbumAndTituloIgnoreCase(
                albumSalvo,
                "Bohemian Rhapsody"
        )).thenReturn(false);

        when(generoRepository.findByNomeIgnoreCase("Rock"))
                .thenReturn(Optional.empty());

        when(generoRepository.save(any(Genero.class)))
                .thenReturn(generoSalvo);

        when(musicaRepository.save(any(Musica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MusicaResponseDTO response = musicaService.cadastrarMusica(request);

        assertThat(response.titulo()).isEqualTo("Bohemian Rhapsody");
        assertThat(response.duracaoSegundos()).isEqualTo(354);
        assertThat(response.artistaPrincipal().nome()).isEqualTo("Queen");
        assertThat(response.artistaPrincipal().nomeCompleto()).isEqualTo("Queen");
        assertThat(response.album().titulo()).isEqualTo("A Night at the Opera");
        assertThat(response.generos()).hasSize(1);

        verify(artistaService).buscarEntidadePorId(1L);
        verify(musicaRepository).save(any(Musica.class));
    }

    @Test
    void deveLancarExcecaoQuandoArtistaPrincipalNaoInformado() {
        MusicaRequestDTO request = new MusicaRequestDTO(
                "Título",
                null,
                200,
                (short) 2020,
                null,
                Set.of(),
                null,
                Set.of("Pop")
        );

        assertThatThrownBy(() -> musicaService.cadastrarMusica(request))
                .isInstanceOf(DadosMusicaInvalidosException.class)
                .hasMessage("O ID do artista principal deve ser válido.");

        verify(musicaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoArtistaParticipanteEhIgualAoPrincipal() {
        MusicaRequestDTO request = new MusicaRequestDTO(
                "Título",
                null,
                200,
                (short) 2020,
                1L,
                Set.of(1L),
                null,
                Set.of("Rock")
        );

        assertThatThrownBy(() -> musicaService.cadastrarMusica(request))
                .isInstanceOf(DadosMusicaInvalidosException.class)
                .hasMessage("O artista principal não pode aparecer como participante.");

        verify(musicaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoMusicaJaEstaCadastradaNoMesmoAlbum() {
        MusicaRequestDTO request = montarRequestValida();

        Artista artistaExistente = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );
        Album albumExistente = new Album(
                artistaExistente,
                "A Night at the Opera",
                (short) 1975,
                null
        );

        when(artistaService.buscarEntidadePorId(1L))
                .thenReturn(artistaExistente);

        when(albumRepository.findByTituloIgnoreCaseAndArtistaAndAnoLancamento(
                "A Night at the Opera",
                artistaExistente,
                (short) 1975
        )).thenReturn(Optional.of(albumExistente));

        when(musicaRepository.existsByAlbumAndTituloIgnoreCase(
                albumExistente,
                "Bohemian Rhapsody"
        )).thenReturn(true);

        assertThatThrownBy(() -> musicaService.cadastrarMusica(request))
                .isInstanceOf(MusicaDuplicadaException.class)
                .hasMessage("A música já está cadastrada.");

        verify(musicaRepository, never()).save(any());
    }

    @Test
    void deveListarMusicasCadastradas() {
        Artista artista = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );
        Musica musica = new Musica(
                "Bohemian Rhapsody",
                null,
                354,
                (short) 1975,
                artista,
                null,
                Set.of(),
                Set.of()
        );

        when(musicaRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(musica));

        List<MusicaResponseDTO> resultado = musicaService.listarMusicas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().titulo()).isEqualTo("Bohemian Rhapsody");
    }

    @Test
    void deveBuscarMusicaPorId() {
        Artista artista = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );
        Musica musica = new Musica(
                "Bohemian Rhapsody",
                null,
                354,
                (short) 1975,
                artista,
                null,
                Set.of(),
                Set.of()
        );

        when(musicaRepository.findById(1L))
                .thenReturn(Optional.of(musica));

        MusicaResponseDTO resultado = musicaService.buscarPorId(1L);

        assertThat(resultado.titulo()).isEqualTo("Bohemian Rhapsody");
    }

    @Test
    void deveLancarExcecaoQuandoMusicaNaoEncontradaPorId() {
        when(musicaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> musicaService.buscarPorId(99L))
                .isInstanceOf(MusicaNaoEncontradaException.class);
    }

    private Musica montarMusicaCompleta() {
        Artista artista = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );
        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                "http://capa.png"
        );

        return new Musica(
                "Bohemian Rhapsody",
                null,
                354,
                (short) 1975,
                artista,
                album,
                Set.of(),
                Set.of(new Genero("Rock"))
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void devePesquisarMusicasAplicandoPaginacaoPadraoQuandoParametrosNaoInformados() {
        Musica musica = montarMusicaCompleta();
        Page<Musica> pagina = new PageImpl<>(
                List.of(musica),
                PageRequest.of(0, 20),
                1
        );

        when(musicaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pagina);

        PaginaResponseDTO<MusicaListagemDTO> resultado =
                musicaService.pesquisarMusicas(null, null, null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(musicaRepository).findAll(any(Specification.class), captor.capture());

        Pageable pageableUsado = captor.getValue();
        assertThat(pageableUsado.getPageNumber()).isEqualTo(0);
        assertThat(pageableUsado.getPageSize()).isEqualTo(20);
        assertThat(pageableUsado.getSort().getOrderFor("titulo")).isNotNull();

        assertThat(resultado.totalItens()).isEqualTo(1);
        assertThat(resultado.itens()).hasSize(1);

        MusicaListagemDTO item = resultado.itens().getFirst();
        assertThat(item.titulo()).isEqualTo("Bohemian Rhapsody");
        assertThat(item.artistaPrincipal().nome()).isEqualTo("Queen");
        assertThat(item.album().titulo()).isEqualTo("A Night at the Opera");
        assertThat(item.generos()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void deveLimitarTamanhoDaPaginaAoMaximoPermitidoQuandoValorPedidoForMaior() {
        when(musicaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        musicaService.pesquisarMusicas(null, 0, 500);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(musicaRepository).findAll(any(Specification.class), captor.capture());

        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void deveLancarExcecaoQuandoPaginaForNegativa() {
        assertThatThrownBy(() -> musicaService.pesquisarMusicas(null, -1, 10))
                .isInstanceOf(DadosMusicaInvalidosException.class);

        verify(musicaRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deveLancarExcecaoQuandoTamanhoDaPaginaForZeroOuNegativo() {
        assertThatThrownBy(() -> musicaService.pesquisarMusicas(null, 0, 0))
                .isInstanceOf(DadosMusicaInvalidosException.class);
    }

    @Test
    void deveLancarExcecaoQuandoAnoDoFiltroForInvalido() {
        MusicaFiltroDTO filtroComAnoFuturo = new MusicaFiltroDTO(null, null, null, null, (short) 3000);

        assertThatThrownBy(() -> musicaService.pesquisarMusicas(filtroComAnoFuturo, 0, 10))
                .isInstanceOf(DadosMusicaInvalidosException.class);

        verify(musicaRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }
}