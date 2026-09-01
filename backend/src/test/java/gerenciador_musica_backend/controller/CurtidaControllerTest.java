package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.service.CurtidaService;
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

@WebMvcTest(CurtidaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CurtidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurtidaService curtidaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveCurtirMusica() throws Exception {
        doNothing().when(curtidaService).curtirMusica(5L);

        mockMvc.perform(post("/api/musicas/5/curtida"))
                .andExpect(status().isNoContent());

        verify(curtidaService).curtirMusica(5L);
    }

    @Test
    void deveDescurtirMusica() throws Exception {
        doNothing().when(curtidaService).descurtirMusica(5L);

        mockMvc.perform(delete("/api/musicas/5/curtida"))
                .andExpect(status().isNoContent());

        verify(curtidaService).descurtirMusica(5L);
    }

    @Test
    void deveCurtirAlbum() throws Exception {
        doNothing().when(curtidaService).curtirAlbum(9L);

        mockMvc.perform(post("/api/albuns/9/curtida"))
                .andExpect(status().isNoContent());

        verify(curtidaService).curtirAlbum(9L);
    }

    @Test
    void deveDescurtirAlbum() throws Exception {
        doNothing().when(curtidaService).descurtirAlbum(9L);

        mockMvc.perform(delete("/api/albuns/9/curtida"))
                .andExpect(status().isNoContent());

        verify(curtidaService).descurtirAlbum(9L);
    }
}
