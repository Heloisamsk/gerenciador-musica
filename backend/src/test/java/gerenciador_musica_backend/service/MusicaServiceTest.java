package gerenciador_musica_backend.service;

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
 * Teste de UNIDADE do MusicaService: as dependências são mockadas,
 * portanto os testes não acessam um banco de dados real.
 */
@ExtendWith(MockitoExtension.class)
class MusicaServiceTest {

    @Mock
    private MusicaRepository musicaRepository;

    @Mock
    private AlbumService albumService;

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
                1L,
                Set.of("Rock")
        );
    }

    @Test
    void deveCadastrarMusicaComSucessoUsandoArtistaAlbumEGenero() {
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

        when(albumService.buscarAlbumDoArtista(1L, artistaExistente))
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

        when(albumService.buscarAlbumDoArtista(1L, artistaExistente))
                .thenReturn(albumExistente);

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
    void deveAtualizarTodosOsDadosEAssociacoesNaMesmaEntidade() {
        Artista artistaAnterior = montarArtista(1L, "Artista anterior");
        Artista artistaNovo = montarArtista(2L, "Artista novo");
        Artista participante = montarArtista(3L, "Participante");
        Album albumAnterior = montarAlbum(
                1L,
                artistaAnterior,
                "Álbum anterior"
        );
        Album albumNovo = montarAlbum(2L, artistaNovo, "Álbum novo");
        Genero generoNovo = new Genero("Pop");
        generoNovo.setIdGenero(2L);
        Musica musica = new Musica(
                "Título anterior",
                null,
                180,
                (short) 2020,
                artistaAnterior,
                albumAnterior
        );
        musica.setIdMusica(10L);
        musica.setGeneros(Set.of(new Genero("Rock")));

        MusicaRequestDTO request = new MusicaRequestDTO(
                "  Música   atualizada  ",
                "  Texto de teste  ",
                240,
                (short) 2024,
                2L,
                Set.of(3L),
                2L,
                Set.of("  Pop  ")
        );

        when(musicaRepository.findById(10L))
                .thenReturn(Optional.of(musica));
        when(artistaService.buscarEntidadePorId(2L))
                .thenReturn(artistaNovo);
        when(artistaService.buscarEntidadePorId(3L))
                .thenReturn(participante);
        when(albumService.buscarAlbumDoArtista(2L, artistaNovo))
                .thenReturn(albumNovo);
        when(musicaRepository
                .existsByAlbumAndTituloIgnoreCaseAndIdMusicaNot(
                        albumNovo,
                        "Música atualizada",
                        10L
                )).thenReturn(false);
        when(generoRepository.findByNomeIgnoreCase("Pop"))
                .thenReturn(Optional.of(generoNovo));

        MusicaResponseDTO response = musicaService.atualizarMusica(
                10L,
                request
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.titulo()).isEqualTo("Música atualizada");
        assertThat(response.letra()).isEqualTo("Texto de teste");
        assertThat(response.duracaoSegundos()).isEqualTo(240);
        assertThat(response.anoLancamento()).isEqualTo((short) 2024);
        assertThat(response.artistaPrincipal().id()).isEqualTo(2L);
        assertThat(response.album().id()).isEqualTo(2L);
        assertThat(response.artistasParticipantes())
                .extracting(artista -> artista.id())
                .containsExactly(3L);
        assertThat(response.generos())
                .extracting(genero -> genero.nome())
                .containsExactly("Pop");
        assertThat(musica.getIdMusica()).isEqualTo(10L);
        assertThat(musica.getArtistaPrincipal()).isSameAs(artistaNovo);
        assertThat(musica.getAlbum()).isSameAs(albumNovo);

        verify(musicaRepository, never()).save(any());
    }

    @Test
    void deveManterTituloAtualERemoverAlbumSemFalsoConflito() {
        Artista artista = montarArtista(1L, "Artista");
        Album album = montarAlbum(1L, artista, "Álbum");
        Genero genero = new Genero("Rock");
        Musica musica = new Musica(
                "Mesmo título",
                null,
                200,
                (short) 2022,
                artista,
                album
        );
        musica.setIdMusica(10L);
        musica.setGeneros(Set.of(genero));
        MusicaRequestDTO request = new MusicaRequestDTO(
                "Mesmo título",
                null,
                200,
                (short) 2022,
                1L,
                Set.of(),
                null,
                Set.of("Rock")
        );

        when(musicaRepository.findById(10L))
                .thenReturn(Optional.of(musica));
        when(artistaService.buscarEntidadePorId(1L))
                .thenReturn(artista);
        when(albumService.buscarAlbumDoArtista(null, artista))
                .thenReturn(null);
        when(musicaRepository
                .existsByAlbumIsNullAndArtistaPrincipalAndTituloIgnoreCaseAndAnoLancamentoAndIdMusicaNot(
                        artista,
                        "Mesmo título",
                        (short) 2022,
                        10L
                )).thenReturn(false);
        when(generoRepository.findByNomeIgnoreCase("Rock"))
                .thenReturn(Optional.of(genero));

        MusicaResponseDTO response = musicaService.atualizarMusica(
                10L,
                request
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.titulo()).isEqualTo("Mesmo título");
        assertThat(response.album()).isNull();
        assertThat(musica.getAlbum()).isNull();
    }

    @Test
    void deveBloquearDuplicidadePertencenteAOutraMusica() {
        Artista artista = montarArtista(1L, "Artista");
        Album album = montarAlbum(1L, artista, "Álbum");
        Musica musica = new Musica(
                "Título anterior",
                null,
                180,
                (short) 2020,
                artista,
                album
        );
        musica.setIdMusica(10L);
        MusicaRequestDTO request = montarRequestValida();

        when(musicaRepository.findById(10L))
                .thenReturn(Optional.of(musica));
        when(artistaService.buscarEntidadePorId(1L))
                .thenReturn(artista);
        when(albumService.buscarAlbumDoArtista(1L, artista))
                .thenReturn(album);
        when(musicaRepository
                .existsByAlbumAndTituloIgnoreCaseAndIdMusicaNot(
                        album,
                        "Bohemian Rhapsody",
                        10L
                )).thenReturn(true);

        assertThatThrownBy(() -> musicaService.atualizarMusica(
                10L,
                request
        ))
                .isInstanceOf(MusicaDuplicadaException.class)
                .hasMessage("A música já está cadastrada.");

        assertThat(musica.getTitulo()).isEqualTo("Título anterior");
        verify(generoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoAtualizarMusicaInexistente() {
        when(musicaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> musicaService.atualizarMusica(
                99L,
                montarRequestValida()
        ))
                .isInstanceOf(MusicaNaoEncontradaException.class)
                .hasMessage("Música não encontrada com o ID: 99");

        verify(artistaService, never()).buscarEntidadePorId(any());
    }

    @Test
    void deveExcluirMusicaExistente() {
        Artista artista = montarArtista(1L, "Artista");
        Musica musica = new Musica(
                "Música",
                null,
                180,
                (short) 2020,
                artista,
                null
        );
        musica.setIdMusica(10L);

        when(musicaRepository.findById(10L))
                .thenReturn(Optional.of(musica));

        musicaService.excluirMusica(10L);

        verify(musicaRepository).delete(musica);
    }

    @Test
    void deveRejeitarIdInvalidoOuMusicaInexistenteNaExclusao() {
        assertThatThrownBy(() -> musicaService.excluirMusica(0L))
                .isInstanceOf(DadosMusicaInvalidosException.class)
                .hasMessage("O ID da música deve ser positivo.");

        when(musicaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> musicaService.excluirMusica(99L))
                .isInstanceOf(MusicaNaoEncontradaException.class)
                .hasMessage("Música não encontrada com o ID: 99");

        verify(musicaRepository, never()).delete(any(Musica.class));
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
                null
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

    private Artista montarArtista(Long id, String nome) {
        Artista artista = new Artista(
                nome,
                nome + " completo",
                "Descrição de teste.",
                null
        );
        artista.setIdArtista(id);

        return artista;
    }

    private Album montarAlbum(
            Long id,
            Artista artista,
            String titulo
    ) {
        Album album = new Album(
                artista,
                titulo,
                (short) 2024,
                null
        );
        album.setIdAlbum(id);

        return album;
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

        Musica musica = new Musica(
                "Bohemian Rhapsody",
                null,
                354,
                (short) 1975,
                artista,
                album
        );
        musica.setGeneros(Set.of(new Genero("Rock")));

        return musica;
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
                musicaService.pesquisarMusicas(null, null, null, null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(musicaRepository).findAll(any(Specification.class), captor.capture());

        Pageable pageableUsado = captor.getValue();
        assertThat(pageableUsado.getPageNumber()).isZero();
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

        musicaService.pesquisarMusicas(null, 0, 500, null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(musicaRepository).findAll(any(Specification.class), captor.capture());

        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void deveLancarExcecaoQuandoPaginaForNegativa() {
        assertThatThrownBy(() -> musicaService.pesquisarMusicas(null, -1, 10, null))
                .isInstanceOf(DadosMusicaInvalidosException.class);

        verify(musicaRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deveLancarExcecaoQuandoTamanhoDaPaginaForZeroOuNegativo() {
        assertThatThrownBy(() -> musicaService.pesquisarMusicas(null, 0, 0, null))
                .isInstanceOf(DadosMusicaInvalidosException.class);
    }

    @Test
    void deveLancarExcecaoQuandoAnoDoFiltroForInvalido() {
        MusicaFiltroDTO filtroComAnoFuturo = new MusicaFiltroDTO(null, null, null, null, (short) 3000);

        assertThatThrownBy(() -> musicaService.pesquisarMusicas(filtroComAnoFuturo, 0, 10, null))
                .isInstanceOf(DadosMusicaInvalidosException.class);

        verify(musicaRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void deveOrdenarPorCampoEDirecaoInformadosNoSort() {
        when(musicaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        musicaService.pesquisarMusicas(null, 0, 10, "anoLancamento,desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(musicaRepository).findAll(any(Specification.class), captor.capture());

        Sort.Order ordem = captor.getValue().getSort().getOrderFor("anoLancamento");
        assertThat(ordem).isNotNull();
        assertThat(ordem.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void deveLancarExcecaoQuandoCampoDeOrdenacaoForInvalido() {
        assertThatThrownBy(() -> musicaService.pesquisarMusicas(null, 0, 10, "senha,asc"))
                .isInstanceOf(DadosMusicaInvalidosException.class);

        verify(musicaRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }
}
