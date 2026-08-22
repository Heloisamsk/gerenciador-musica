package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.exception.ArtistaEmUsoException;
import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosArtistaInvalidosException;
import gerenciador_musica_backend.service.ArtistaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminArtistaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminArtistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistaService artistaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveExcluirArtistaComStatus204() throws Exception {
        mockMvc.perform(delete("/api/admin/artistas/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(artistaService).excluirArtista(1L);
    }

    @Test
    void deveRetornar400QuandoIdForInvalido() throws Exception {
        doThrow(new DadosArtistaInvalidosException(
                "O ID do artista deve ser positivo."
        )).when(artistaService).excluirArtista(0L);

        mockMvc.perform(delete("/api/admin/artistas/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("O ID do artista deve ser positivo."));
    }

    @Test
    void deveRetornar404QuandoArtistaNaoExistir() throws Exception {
        doThrow(new ArtistaNaoEncontradoException(99L))
                .when(artistaService)
                .excluirArtista(99L);

        mockMvc.perform(delete("/api/admin/artistas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Artista não encontrado com o ID: 99"));
    }

    @Test
    void deveRetornar409QuandoArtistaEstiverEmUso() throws Exception {
        String mensagem = "Não é possível excluir o artista porque "
                + "ele possui álbuns associados.";

        doThrow(new ArtistaEmUsoException(mensagem))
                .when(artistaService)
                .excluirArtista(1L);

        mockMvc.perform(delete("/api/admin/artistas/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(mensagem));
    }

    @Test
    void deveRetornar400QuandoIdNaoForNumerico() throws Exception {
        mockMvc.perform(delete("/api/admin/artistas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("O parâmetro 'idArtista' possui um valor inválido."));
    }
}
