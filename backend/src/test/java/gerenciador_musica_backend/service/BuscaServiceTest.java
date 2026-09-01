package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.dto.BuscaResultadoDTO;
import gerenciador_musica_backend.dto.MusicaFiltroDTO;
import gerenciador_musica_backend.dto.MusicaListagemDTO;
import gerenciador_musica_backend.dto.PaginaResponseDTO;
import gerenciador_musica_backend.dto.UsuarioBuscaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscaServiceTest {

    @Mock
    private MusicaService musicaService;
    @Mock
    private AlbumService albumService;
    @Mock
    private ArtistaService artistaService;
    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private BuscaService buscaService;

    @Test
    void deveRetornarResultadoVazioParaTermoNuloOuEmBranco() {
        BuscaResultadoDTO resultadoNulo = buscaService.buscar(null);
        BuscaResultadoDTO resultadoBranco = buscaService.buscar("   ");

        assertThat(resultadoNulo.musicas()).isEmpty();
        assertThat(resultadoNulo.albuns()).isEmpty();
        assertThat(resultadoNulo.artistas()).isEmpty();
        assertThat(resultadoNulo.usuarios()).isEmpty();
        assertThat(resultadoBranco.musicas()).isEmpty();

        verify(musicaService, never()).pesquisarMusicas(any(), any(), any(), any());
    }

    @Test
    void deveAgregarResultadosDeTodosOsServicos() {
        MusicaListagemDTO musica = new MusicaListagemDTO(
                1L, "Bohemian Rhapsody", 354, (short) 1975,
                null, null, Set.of(), Set.of(), false
        );
        AlbumResponseDTO album = new AlbumResponseDTO(
                1L, "A Night at the Opera", (short) 1975, null, null, false
        );
        ArtistaResponseDTO artista = new ArtistaResponseDTO(
                1L, "Queen", "Queen", "Banda britânica.", null
        );
        UsuarioBuscaDTO usuario = new UsuarioBuscaDTO(1L, "Maria", "maria");

        when(musicaService.pesquisarMusicas(any(MusicaFiltroDTO.class), eq(0), anyInt(), eq(null)))
                .thenReturn(new PaginaResponseDTO<>(List.of(musica), 0, 5, 1, 1));
        when(albumService.buscarPorTitulo(eq("queen"), anyInt()))
                .thenReturn(List.of(album));
        when(artistaService.buscarPorNome(eq("queen"), anyInt()))
                .thenReturn(List.of(artista));
        when(usuarioService.buscarPorNomeOuUsername(eq("queen"), anyInt()))
                .thenReturn(List.of(usuario));

        BuscaResultadoDTO resultado = buscaService.buscar("  queen  ");

        assertThat(resultado.musicas()).containsExactly(musica);
        assertThat(resultado.albuns()).containsExactly(album);
        assertThat(resultado.artistas()).containsExactly(artista);
        assertThat(resultado.usuarios()).containsExactly(usuario);
    }
}
