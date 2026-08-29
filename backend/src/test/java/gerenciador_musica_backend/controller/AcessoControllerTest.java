package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.PerfilResponseDTO;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.service.PerfilService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcessoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AcessoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PerfilService perfilService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveRetornarPerfilSemExporEmail() throws Exception {
        when(perfilService.obterPerfil(isNull())).thenReturn(new PerfilResponseDTO(
                1L,
                "analiz",
                "Ana Liz",
                null,
                Role.USER,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        mockMvc.perform(get("/api/user/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Liz"))
                .andExpect(jsonPath("$.username").value("analiz"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void deveValidarUrlDoBannerNaAtualizacao() throws Exception {
        mockMvc.perform(
                        put("/api/user/perfil")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nome": "Ana Liz",
                                            "bannerUrl": "banner-sem-protocolo.jpg"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bannerUrl").exists());
    }
}
