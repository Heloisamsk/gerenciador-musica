package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.service.AlbumService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlbumController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private AlbumResponseDTO montarResposta() {
        return new AlbumResponseDTO(
                10L,
                "A Night at the Opera",
                (short) 1975,
                null,
                new ArtistaResumoDTO(
                        1L,
                        "Queen",
                        "Queen",
                        "Banda britânica de rock.",
                        null
                )
        );
    }

    @Test
    void deveListarTodosOsAlbunsQuandoFiltroNaoForInformado()
            throws Exception {
        when(albumService.listarAlbuns())
                .thenReturn(List.of(montarResposta()));

        mockMvc.perform(get("/api/albuns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAlbum").value(10))
                .andExpect(jsonPath("$[0].titulo")
                        .value("A Night at the Opera"));

        verify(albumService).listarAlbuns();
    }

    @Test
    void deveListarAlbunsFiltradosPorArtista() throws Exception {
        when(albumService.listarAlbunsPorArtista(1L))
                .thenReturn(List.of(montarResposta()));

        mockMvc.perform(get("/api/albuns")
                        .param("artistaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo")
                        .value("A Night at the Opera"))
                .andExpect(jsonPath("$[0].artista.id")
                        .value(1));

        verify(albumService).listarAlbunsPorArtista(1L);
    }

    @Test
    void deveBuscarAlbumPorId() throws Exception {
        when(albumService.buscarPorId(10L))
                .thenReturn(montarResposta());

        mockMvc.perform(get("/api/albuns/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlbum").value(10));
    }

    @Test
    void deveRetornar404QuandoAlbumNaoExistir() throws Exception {
        when(albumService.buscarPorId(99L))
                .thenThrow(new AlbumNaoEncontradoException(99L));

        mockMvc.perform(get("/api/albuns/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Álbum não encontrado com o ID: 99"));
    }
}
