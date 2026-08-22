package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosArtistaInvalidosException;
import gerenciador_musica_backend.service.ArtistaService;
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

@WebMvcTest(ArtistaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArtistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistaService artistaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ArtistaResponseDTO montarResposta() {
        return new ArtistaResponseDTO(
                1L,
                "Queen",
                "Queen",
                "Banda britânica de rock.",
                "https://exemplo.com/queen.jpg"
        );
    }

    @Test
    void deveListarArtistas() throws Exception {
        when(artistaService.listarArtistas())
                .thenReturn(List.of(montarResposta()));

        mockMvc.perform(get("/api/artistas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idArtista").value(1))
                .andExpect(jsonPath("$[0].nome").value("Queen"))
                .andExpect(jsonPath("$[0].nomeCompleto").value("Queen"))
                .andExpect(jsonPath("$[0].descricao")
                        .value("Banda britânica de rock."))
                .andExpect(jsonPath("$[0].fotoPerfilUrl")
                        .value("https://exemplo.com/queen.jpg"));

        verify(artistaService).listarArtistas();
    }

    @Test
    void deveBuscarArtistaPorId() throws Exception {
        when(artistaService.buscarPorId(1L))
                .thenReturn(montarResposta());

        mockMvc.perform(get("/api/artistas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idArtista").value(1))
                .andExpect(jsonPath("$.nome").value("Queen"))
                .andExpect(jsonPath("$.nomeCompleto").value("Queen"))
                .andExpect(jsonPath("$.descricao")
                        .value("Banda britânica de rock."))
                .andExpect(jsonPath("$.fotoPerfilUrl")
                        .value("https://exemplo.com/queen.jpg"));

        verify(artistaService).buscarPorId(1L);
    }

    @Test
    void deveRetornar404QuandoArtistaNaoExistir() throws Exception {
        when(artistaService.buscarPorId(99L))
                .thenThrow(new ArtistaNaoEncontradoException(99L));

        mockMvc.perform(get("/api/artistas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Artista não encontrado com o ID: 99"));
    }

    @Test
    void deveRetornar400QuandoIdNaoForPositivo() throws Exception {
        when(artistaService.buscarPorId(0L))
                .thenThrow(new DadosArtistaInvalidosException(
                        "O ID do artista deve ser positivo."
                ));

        mockMvc.perform(get("/api/artistas/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("O ID do artista deve ser positivo."));
    }

    @Test
    void deveRetornar400QuandoIdNaoForNumerico() throws Exception {
        mockMvc.perform(get("/api/artistas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("O parâmetro 'idArtista' possui um valor inválido."));
    }
}
