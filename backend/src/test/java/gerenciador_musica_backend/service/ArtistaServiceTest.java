package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.ArtistaRequestDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.exception.ArtistaDuplicadoException;
import gerenciador_musica_backend.exception.ArtistaEmUsoException;
import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosArtistaInvalidosException;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.projection.AlbumCatalogoProjection;
import gerenciador_musica_backend.repository.projection.ArtistaCatalogoResumoProjection;
import gerenciador_musica_backend.repository.projection.MusicaCatalogoProjection;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Teste de unidade do ArtistaService. O ArtistaRepository é mockado,
 * portanto os testes não acessam um banco de dados real.
 */
@ExtendWith(MockitoExtension.class)
class ArtistaServiceTest {

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private MusicaRepository musicaRepository;

    @InjectMocks
    private ArtistaService artistaService;

    private ArtistaRequestDTO montarRequestValida() {
        return new ArtistaRequestDTO(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                "https://exemplo.com/queen.jpg"
        );
    }

    @Test
    void deveCadastrarArtistaComSucesso() {
        ArtistaRequestDTO request = montarRequestValida();

        when(artistaRepository.existsByNomeIgnoreCase("Queen"))
                .thenReturn(false);

        when(artistaRepository.save(any(Artista.class)))
                .thenAnswer(invocation -> {
                    Artista artista = invocation.getArgument(0);
                    artista.setIdArtista(1L);
                    return artista;
                });

        ArtistaResponseDTO response = artistaService.cadastrarArtista(request);

        assertThat(response.idArtista()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Queen");
        assertThat(response.nomeCompleto()).isEqualTo("Queen");
        assertThat(response.descricao()).isEqualTo("Banda britânica de rock.");
        assertThat(response.fotoPerfilUrl())
                .isEqualTo("https://exemplo.com/queen.jpg");

        verify(artistaRepository).existsByNomeIgnoreCase("Queen");
        verify(artistaRepository).save(any(Artista.class));
    }

    @Test
    void deveRemoverEspacosExternosAntesDeSalvar() {
        ArtistaRequestDTO request = new ArtistaRequestDTO(
                "  Queen  ",
                "  Queen  ",
                "  Banda britânica de rock.  ",
                "  https://exemplo.com/queen.jpg  "
        );

        when(artistaRepository.existsByNomeIgnoreCase("Queen"))
                .thenReturn(false);

        when(artistaRepository.save(any(Artista.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArtistaResponseDTO response = artistaService.cadastrarArtista(request);

        assertThat(response.nome()).isEqualTo("Queen");
        assertThat(response.nomeCompleto()).isEqualTo("Queen");
        assertThat(response.descricao()).isEqualTo("Banda britânica de rock.");
        assertThat(response.fotoPerfilUrl())
                .isEqualTo("https://exemplo.com/queen.jpg");

        verify(artistaRepository).existsByNomeIgnoreCase("Queen");
    }

    @Test
    void deveConverterFotoVaziaParaNull() {
        ArtistaRequestDTO request = new ArtistaRequestDTO(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                "   "
        );

        when(artistaRepository.existsByNomeIgnoreCase("Queen"))
                .thenReturn(false);

        when(artistaRepository.save(any(Artista.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArtistaResponseDTO response = artistaService.cadastrarArtista(request);

        assertThat(response.fotoPerfilUrl()).isNull();
    }

    @Test
    void deveLancarExcecaoQuandoRequestForNull() {
        assertThatThrownBy(() -> artistaService.cadastrarArtista(null))
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("Os dados do artista são obrigatórios");

        verify(artistaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNomeArtisticoForNull() {
        ArtistaRequestDTO request = new ArtistaRequestDTO(
                null,
                "Queen",
                "Banda britânica de rock.",
                null
        );

        assertThatThrownBy(() -> artistaService.cadastrarArtista(request))
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("Nome Artístico é obrigatório");

        verify(artistaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNomeCompletoFicarVazio() {
        ArtistaRequestDTO request = new ArtistaRequestDTO(
                "Queen",
                "   ",
                "Banda britânica de rock.",
                null
        );

        assertThatThrownBy(() -> artistaService.cadastrarArtista(request))
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("Nome Completo não pode ficar vazio");

        verify(artistaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoDescricaoFicarVazia() {
        ArtistaRequestDTO request = new ArtistaRequestDTO(
                "Queen",
                "Queen",
                "   ",
                null
        );

        assertThatThrownBy(() -> artistaService.cadastrarArtista(request))
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("Descrição do Artista não pode ficar vazio");

        verify(artistaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoArtistaJaEstiverCadastrado() {
        ArtistaRequestDTO request = montarRequestValida();

        when(artistaRepository.existsByNomeIgnoreCase("Queen"))
                .thenReturn(true);

        assertThatThrownBy(() -> artistaService.cadastrarArtista(request))
                .isInstanceOf(ArtistaDuplicadoException.class)
                .hasMessage("Esse artista já foi cadastrado: Queen");

        verify(artistaRepository).existsByNomeIgnoreCase("Queen");
        verify(artistaRepository, never()).save(any());
    }

    @Test
    void deveListarArtistasOrdenadosPorNome() {
        Artista beatles = new Artista(
                "The Beatles",
                "The Beatles",
                "Banda britânica de rock.",
                null
        );
        beatles.setIdArtista(2L);
        Artista queen = montarArtistaExistente();
        Sort ordenacao = Sort.by(Sort.Direction.ASC, "nome");

        when(artistaRepository.findAll(ordenacao))
                .thenReturn(List.of(queen, beatles));

        List<ArtistaResponseDTO> response =
                artistaService.listarArtistas();

        assertThat(response)
                .extracting(ArtistaResponseDTO::nome)
                .containsExactly("Queen", "The Beatles");
        assertThat(response)
                .extracting(ArtistaResponseDTO::idArtista)
                .containsExactly(1L, 2L);
        verify(artistaRepository).findAll(ordenacao);
    }

    @Test
    void deveBuscarEntidadePorId() {
        Artista artistaExistente = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );

        when(artistaRepository.findById(1L))
                .thenReturn(Optional.of(artistaExistente));

        Artista resultado = artistaService.buscarEntidadePorId(1L);

        assertThat(resultado).isSameAs(artistaExistente);
        verify(artistaRepository).findById(1L);
    }

    @Test
    void deveBuscarArtistaPorIdEConverterParaResponse() {
        Artista artistaExistente = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                "https://exemplo.com/queen.jpg"
        );
        artistaExistente.setIdArtista(1L);

        when(artistaRepository.findById(1L))
                .thenReturn(Optional.of(artistaExistente));

        ArtistaResponseDTO response = artistaService.buscarPorId(1L);

        assertThat(response.idArtista()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Queen");
        assertThat(response.nomeCompleto()).isEqualTo("Queen");
        assertThat(response.descricao())
                .isEqualTo("Banda britânica de rock.");
        assertThat(response.fotoPerfilUrl())
                .isEqualTo("https://exemplo.com/queen.jpg");

        verify(artistaRepository).findById(1L);
    }

    @Test
    void deveMontarDetalhesDoArtistaComAsTresViews() {
        ArtistaCatalogoResumoProjection resumo = mock(
                ArtistaCatalogoResumoProjection.class
        );
        AlbumCatalogoProjection album = mock(
                AlbumCatalogoProjection.class
        );
        MusicaCatalogoProjection musica = mock(
                MusicaCatalogoProjection.class
        );

        when(resumo.getIdArtista()).thenReturn(1L);
        when(resumo.getNome()).thenReturn("Queen");
        when(resumo.getNomeCompleto()).thenReturn("Queen");
        when(resumo.getDescricao()).thenReturn("Banda britânica de rock.");
        when(resumo.getFotoPerfilUrl()).thenReturn(null);
        when(resumo.getTotalAlbuns()).thenReturn(1L);
        when(resumo.getTotalMusicasPrincipais()).thenReturn(1L);
        when(resumo.getTotalParticipacoes()).thenReturn(0L);
        when(resumo.getDuracaoTotalSegundos()).thenReturn(354L);

        when(album.getIdAlbum()).thenReturn(10L);
        when(album.getIdArtista()).thenReturn(1L);
        when(album.getNomeArtista()).thenReturn("Queen");
        when(album.getTitulo()).thenReturn("A Night at the Opera");
        when(album.getAnoLancamento()).thenReturn((short) 1975);
        when(album.getCapaUrl()).thenReturn(null);
        when(album.getTotalMusicas()).thenReturn(1L);
        when(album.getDuracaoTotalSegundos()).thenReturn(354L);

        when(musica.getIdMusica()).thenReturn(20L);
        when(musica.getTitulo()).thenReturn("Bohemian Rhapsody");
        when(musica.getDuracaoSegundos()).thenReturn(354);
        when(musica.getAnoLancamento()).thenReturn((short) 1975);
        when(musica.getIdArtistaPrincipal()).thenReturn(1L);
        when(musica.getNomeArtistaPrincipal()).thenReturn("Queen");
        when(musica.getIdAlbum()).thenReturn(10L);
        when(musica.getTituloAlbum()).thenReturn("A Night at the Opera");
        when(musica.getCapaUrl()).thenReturn(null);
        when(musica.getGeneros()).thenReturn("Rock, Rock Progressivo");
        when(musica.getPapelArtista()).thenReturn("PRINCIPAL");

        when(artistaRepository.buscarResumoCatalogo(1L))
                .thenReturn(Optional.of(resumo));
        when(albumRepository.buscarCatalogoPorArtista(1L))
                .thenReturn(List.of(album));
        when(musicaRepository.buscarCatalogoPorArtista(1L))
                .thenReturn(List.of(musica));

        var detalhes = artistaService.buscarDetalhesCatalogo(1L);

        assertThat(detalhes.artista().nome()).isEqualTo("Queen");
        assertThat(detalhes.artista().totalAlbuns()).isEqualTo(1L);
        assertThat(detalhes.albuns())
                .singleElement()
                .satisfies(item -> assertThat(item.titulo())
                        .isEqualTo("A Night at the Opera"));
        assertThat(detalhes.musicas())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.titulo())
                            .isEqualTo("Bohemian Rhapsody");
                    assertThat(item.generos())
                            .containsExactly("Rock", "Rock Progressivo");
                    assertThat(item.papelArtista())
                            .isEqualTo("PRINCIPAL");
                });

        verify(artistaRepository).buscarResumoCatalogo(1L);
        verify(albumRepository).buscarCatalogoPorArtista(1L);
        verify(musicaRepository).buscarCatalogoPorArtista(1L);
    }

    @Test
    void deveRejeitarIdInvalidoAoBuscarDetalhesDoCatalogo() {
        assertThatThrownBy(
                () -> artistaService.buscarDetalhesCatalogo(0L)
        )
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("O ID do artista deve ser positivo.");

        verify(artistaRepository, never()).buscarResumoCatalogo(any());
        verify(albumRepository, never()).buscarCatalogoPorArtista(any());
        verify(musicaRepository, never()).buscarCatalogoPorArtista(any());
    }

    @Test
    void deveInformarQuandoArtistaDosDetalhesNaoExistir() {
        when(artistaRepository.buscarResumoCatalogo(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> artistaService.buscarDetalhesCatalogo(99L)
        )
                .isInstanceOf(ArtistaNaoEncontradoException.class)
                .hasMessage("Artista não encontrado com o ID: 99");

        verify(albumRepository, never()).buscarCatalogoPorArtista(any());
        verify(musicaRepository, never()).buscarCatalogoPorArtista(any());
    }

    @Test
    void deveLancarExcecaoQuandoBuscarPorIdNaoEncontrarArtista() {
        when(artistaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistaService.buscarPorId(99L))
                .isInstanceOf(ArtistaNaoEncontradoException.class)
                .hasMessage("Artista não encontrado com o ID: 99");

        verify(artistaRepository).findById(99L);
    }

    @Test
    void deveLancarExcecaoQuandoIdForNull() {
        assertThatThrownBy(() -> artistaService.buscarPorId(null))
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("O ID do artista deve ser positivo.");

        verify(artistaRepository, never()).findById(any());
    }

    @Test
    void deveLancarExcecaoQuandoIdForZero() {
        assertThatThrownBy(() -> artistaService.buscarPorId(0L))
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("O ID do artista deve ser positivo.");

        verify(artistaRepository, never()).findById(any());
    }

    @Test
    void deveLancarExcecaoQuandoIdForNegativo() {
        assertThatThrownBy(() -> artistaService.buscarPorId(-1L))
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("O ID do artista deve ser positivo.");

        verify(artistaRepository, never()).findById(any());
    }

    @Test
    void deveExcluirArtistaSemDependencias() {
        Artista artista = montarArtistaExistente();

        when(artistaRepository.findById(1L))
                .thenReturn(Optional.of(artista));
        when(albumRepository.existsByArtista_IdArtista(1L))
                .thenReturn(false);
        when(musicaRepository
                .existsByArtistaPrincipal_IdArtista(1L))
                .thenReturn(false);
        when(musicaRepository
                .existsByArtistasParticipantes_IdArtista(1L))
                .thenReturn(false);

        artistaService.excluirArtista(1L);

        verify(albumRepository).existsByArtista_IdArtista(1L);
        verify(musicaRepository)
                .existsByArtistaPrincipal_IdArtista(1L);
        verify(musicaRepository)
                .existsByArtistasParticipantes_IdArtista(1L);
        verify(artistaRepository).delete(artista);
    }

    @Test
    void naoDeveExcluirArtistaComAlbum() {
        Artista artista = montarArtistaExistente();

        when(artistaRepository.findById(1L))
                .thenReturn(Optional.of(artista));
        when(albumRepository.existsByArtista_IdArtista(1L))
                .thenReturn(true);

        assertThatThrownBy(() -> artistaService.excluirArtista(1L))
                .isInstanceOf(ArtistaEmUsoException.class)
                .hasMessage(
                        "Não é possível excluir o artista porque "
                                + "ele possui álbuns associados."
                );

        verify(artistaRepository, never()).delete(any(Artista.class));
        verify(musicaRepository, never())
                .existsByArtistaPrincipal_IdArtista(any());
        verify(musicaRepository, never())
                .existsByArtistasParticipantes_IdArtista(any());
    }

    @Test
    void naoDeveExcluirArtistaPrincipalDeMusica() {
        Artista artista = montarArtistaExistente();

        when(artistaRepository.findById(1L))
                .thenReturn(Optional.of(artista));
        when(albumRepository.existsByArtista_IdArtista(1L))
                .thenReturn(false);
        when(musicaRepository
                .existsByArtistaPrincipal_IdArtista(1L))
                .thenReturn(true);

        assertThatThrownBy(() -> artistaService.excluirArtista(1L))
                .isInstanceOf(ArtistaEmUsoException.class)
                .hasMessage(
                        "Não é possível excluir o artista porque ele é "
                                + "o artista principal de uma ou mais músicas."
                );

        verify(artistaRepository, never()).delete(any(Artista.class));
        verify(musicaRepository, never())
                .existsByArtistasParticipantes_IdArtista(any());
    }

    @Test
    void naoDeveExcluirArtistaParticipanteDeMusica() {
        Artista artista = montarArtistaExistente();

        when(artistaRepository.findById(1L))
                .thenReturn(Optional.of(artista));
        when(albumRepository.existsByArtista_IdArtista(1L))
                .thenReturn(false);
        when(musicaRepository
                .existsByArtistaPrincipal_IdArtista(1L))
                .thenReturn(false);
        when(musicaRepository
                .existsByArtistasParticipantes_IdArtista(1L))
                .thenReturn(true);

        assertThatThrownBy(() -> artistaService.excluirArtista(1L))
                .isInstanceOf(ArtistaEmUsoException.class)
                .hasMessage(
                        "Não é possível excluir o artista porque ele "
                                + "participa de uma ou mais músicas."
                );

        verify(artistaRepository, never()).delete(any(Artista.class));
    }

    @Test
    void naoDeveExcluirArtistaInexistente() {
        when(artistaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistaService.excluirArtista(99L))
                .isInstanceOf(ArtistaNaoEncontradoException.class)
                .hasMessage("Artista não encontrado com o ID: 99");

        verify(albumRepository, never())
                .existsByArtista_IdArtista(any());
        verify(musicaRepository, never())
                .existsByArtistaPrincipal_IdArtista(any());
        verify(musicaRepository, never())
                .existsByArtistasParticipantes_IdArtista(any());
        verify(artistaRepository, never()).delete(any(Artista.class));
    }

    @Test
    void naoDeveConsultarRepositoriosQuandoIdDaExclusaoForInvalido() {
        assertThatThrownBy(() -> artistaService.excluirArtista(0L))
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("O ID do artista deve ser positivo.");

        verify(artistaRepository, never()).findById(any());
        verify(albumRepository, never())
                .existsByArtista_IdArtista(any());
        verify(musicaRepository, never())
                .existsByArtistaPrincipal_IdArtista(any());
        verify(musicaRepository, never())
                .existsByArtistasParticipantes_IdArtista(any());
    }

    @Test
    void deveAtualizarTodosOsCamposDoArtista() {
        Artista artista = montarArtistaExistente();
        ArtistaRequestDTO request = new ArtistaRequestDTO(
                "Queen + Adam Lambert",
                "Queen e Adam Lambert",
                "Projeto musical em atividade.",
                "https://exemplo.com/queen-atualizado.jpg"
        );

        when(artistaRepository.findById(1L))
                .thenReturn(Optional.of(artista));
        when(artistaRepository
                .existsByNomeIgnoreCaseAndIdArtistaNot(
                        "Queen + Adam Lambert",
                        1L
                ))
                .thenReturn(false);

        ArtistaResponseDTO response = artistaService.atualizarArtista(
                1L,
                request
        );

        assertThat(response.idArtista()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Queen + Adam Lambert");
        assertThat(response.nomeCompleto())
                .isEqualTo("Queen e Adam Lambert");
        assertThat(response.descricao())
                .isEqualTo("Projeto musical em atividade.");
        assertThat(response.fotoPerfilUrl())
                .isEqualTo(
                        "https://exemplo.com/queen-atualizado.jpg"
                );
        assertThat(artista.getFotoPerfilUrl())
                .isEqualTo(response.fotoPerfilUrl());
    }

    @Test
    void deveNormalizarDadosERemoverFotoNoMesmoObjetoExistente() {
        Artista artista = montarArtistaExistente();
        ArtistaRequestDTO request = new ArtistaRequestDTO(
                "  Queen + Adam Lambert  ",
                "  Queen e Adam Lambert  ",
                "  Projeto musical em atividade.  ",
                "   "
        );

        when(artistaRepository.findById(1L))
                .thenReturn(Optional.of(artista));
        when(artistaRepository
                .existsByNomeIgnoreCaseAndIdArtistaNot(
                        "Queen + Adam Lambert",
                        1L
                ))
                .thenReturn(false);

        ArtistaResponseDTO response = artistaService.atualizarArtista(
                1L,
                request
        );

        assertThat(response.idArtista()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Queen + Adam Lambert");
        assertThat(response.nomeCompleto())
                .isEqualTo("Queen e Adam Lambert");
        assertThat(response.descricao())
                .isEqualTo("Projeto musical em atividade.");
        assertThat(response.fotoPerfilUrl()).isNull();
        assertThat(artista.getIdArtista()).isEqualTo(1L);
        assertThat(artista.getNome()).isEqualTo(response.nome());
        assertThat(artista.getNomeCompleto())
                .isEqualTo(response.nomeCompleto());
        assertThat(artista.getDescricao())
                .isEqualTo(response.descricao());
        assertThat(artista.getFotoPerfilUrl()).isNull();

        verify(artistaRepository).findById(1L);
        verify(artistaRepository)
                .existsByNomeIgnoreCaseAndIdArtistaNot(
                        "Queen + Adam Lambert",
                        1L
                );
        verify(artistaRepository, never()).save(any(Artista.class));
    }

    @Test
    void devePermitirManterOMesmoNome() {
        Artista artista = montarArtistaExistente();
        ArtistaRequestDTO request = montarRequestValida();

        when(artistaRepository.findById(1L))
                .thenReturn(Optional.of(artista));
        when(artistaRepository
                .existsByNomeIgnoreCaseAndIdArtistaNot("Queen", 1L))
                .thenReturn(false);

        ArtistaResponseDTO response = artistaService.atualizarArtista(
                1L,
                request
        );

        assertThat(response.nome()).isEqualTo("Queen");
        verify(artistaRepository)
                .existsByNomeIgnoreCaseAndIdArtistaNot("Queen", 1L);
    }

    @Test
    void naoDeveAtualizarComNomeDeOutroArtista() {
        Artista artista = montarArtistaExistente();
        ArtistaRequestDTO request = new ArtistaRequestDTO(
                "The Beatles",
                "The Beatles",
                "Banda britânica de rock.",
                null
        );

        when(artistaRepository.findById(1L))
                .thenReturn(Optional.of(artista));
        when(artistaRepository
                .existsByNomeIgnoreCaseAndIdArtistaNot(
                        "The Beatles",
                        1L
                ))
                .thenReturn(true);

        assertThatThrownBy(() -> artistaService.atualizarArtista(
                1L,
                request
        ))
                .isInstanceOf(ArtistaDuplicadoException.class)
                .hasMessage(
                        "Esse artista já foi cadastrado: The Beatles"
                );

        assertThat(artista.getNome()).isEqualTo("Queen");
        assertThat(artista.getNomeCompleto()).isEqualTo("Queen");
        assertThat(artista.getDescricao())
                .isEqualTo("Banda britânica de rock.");
        verify(artistaRepository, never()).save(any(Artista.class));
    }

    @Test
    void naoDeveAtualizarArtistaInexistente() {
        ArtistaRequestDTO request = montarRequestValida();

        when(artistaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistaService.atualizarArtista(
                99L,
                request
        ))
                .isInstanceOf(ArtistaNaoEncontradoException.class)
                .hasMessage("Artista não encontrado com o ID: 99");

        verify(artistaRepository, never())
                .existsByNomeIgnoreCaseAndIdArtistaNot(any(), any());
        verify(artistaRepository, never()).save(any(Artista.class));
    }

    @Test
    void naoDeveAtualizarComRequestNulo() {
        assertThatThrownBy(() -> artistaService.atualizarArtista(
                1L,
                null
        ))
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("Os dados do artista são obrigatórios");

        verify(artistaRepository, never()).findById(any());
    }

    @Test
    void naoDeveAtualizarQuandoIdForInvalido() {
        assertThatThrownBy(() -> artistaService.atualizarArtista(
                0L,
                montarRequestValida()
        ))
                .isInstanceOf(DadosArtistaInvalidosException.class)
                .hasMessage("O ID do artista deve ser positivo.");

        verify(artistaRepository, never()).findById(any());
        verify(artistaRepository, never())
                .existsByNomeIgnoreCaseAndIdArtistaNot(any(), any());
    }

    private Artista montarArtistaExistente() {
        Artista artista = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );
        artista.setIdArtista(1L);

        return artista;
    }
}
