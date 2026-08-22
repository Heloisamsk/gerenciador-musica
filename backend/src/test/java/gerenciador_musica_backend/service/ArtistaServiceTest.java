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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
    void deveLancarExcecaoQuandoArtistaNaoForEncontrado() {
        when(artistaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistaService.buscarEntidadePorId(99L))
                .isInstanceOf(ArtistaNaoEncontradoException.class);

        verify(artistaRepository).findById(99L);
    }

    @Test
    void deveLancarExcecaoQuandoIdForNull() {
        assertThatThrownBy(() -> artistaService.buscarEntidadePorId(null))
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
