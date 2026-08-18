package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumResponseDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/*
 * Teste de unidade do AlbumService. O AlbumRepository é mockado,
 * portanto os testes não acessam um banco de dados real.
 */
@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void deveListarAlbunsCadastrados() {
        Artista artista = new Artista(
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                null
        );
        artista.setIdArtista(1L);

        Album album = new Album(
                artista,
                "A Night at the Opera",
                (short) 1975,
                "http://capa.png"
        );
        album.setIdAlbum(1L);

        when(albumRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(album));

        List<AlbumResponseDTO> resultado = albumService.listarAlbuns();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().titulo()).isEqualTo("A Night at the Opera");
        assertThat(resultado.getFirst().artista().nome()).isEqualTo("Queen");
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaAlbunsCadastrados() {
        when(albumRepository.findAll(any(Sort.class)))
                .thenReturn(List.of());

        List<AlbumResponseDTO> resultado = albumService.listarAlbuns();

        assertThat(resultado).isEmpty();
    }
}
