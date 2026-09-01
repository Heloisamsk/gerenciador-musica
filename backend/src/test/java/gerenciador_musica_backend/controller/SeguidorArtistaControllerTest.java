package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.service.SeguidorArtistaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeguidorArtistaController.class)
@AutoConfigureMockMvc(addFilters = false)
class SeguidorArtistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeguidorArtistaService seguidorArtistaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveSeguirArtista() throws Exception {
        doNothing().when(seguidorArtistaService).seguirArtista(7L);

        mockMvc.perform(post("/api/artistas/7/seguidor"))
                .andExpect(status().isNoContent());

        verify(seguidorArtistaService).seguirArtista(7L);
    }

    @Test
    void deveDeixarDeSeguirArtista() throws Exception {
        doNothing().when(seguidorArtistaService).deixarDeSeguirArtista(7L);

        mockMvc.perform(delete("/api/artistas/7/seguidor"))
                .andExpect(status().isNoContent());

        verify(seguidorArtistaService).deixarDeSeguirArtista(7L);
    }
}
