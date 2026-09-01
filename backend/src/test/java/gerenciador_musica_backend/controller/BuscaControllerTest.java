package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.BuscaResultadoDTO;
import gerenciador_musica_backend.service.BuscaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuscaController.class)
@AutoConfigureMockMvc(addFilters = false)
class BuscaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuscaService buscaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveBuscarPorTermo() throws Exception {
        when(buscaService.buscar("queen")).thenReturn(
                new BuscaResultadoDTO(List.of(), List.of(), List.of(), List.of())
        );

        mockMvc.perform(get("/api/busca").param("q", "queen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.musicas").isArray());
    }

    @Test
    void deveBuscarSemParametro() throws Exception {
        when(buscaService.buscar(null)).thenReturn(
                new BuscaResultadoDTO(List.of(), List.of(), List.of(), List.of())
        );

        mockMvc.perform(get("/api/busca"))
                .andExpect(status().isOk());
    }
}
