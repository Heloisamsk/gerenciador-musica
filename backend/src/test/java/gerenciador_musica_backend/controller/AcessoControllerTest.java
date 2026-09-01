package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.PerfilResponseDTO;
import gerenciador_musica_backend.exception.DadosPerfilInvalidosException;
import gerenciador_musica_backend.exception.UsuarioNaoEncontradoException;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.service.PerfilService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
                null,
                List.of(),
                List.of(),
                List.of(),
                0L,
                0L,
                0L,
                0L,
                false,
                false
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

    @Test
    void deveRetornar400QuandoDadosDoPerfilSaoInvalidos() throws Exception {
        when(perfilService.atualizarPerfil(isNull(), any()))
                .thenThrow(new DadosPerfilInvalidosException(
                        "Escolha um item válido para o destaque principal."
                ));

        mockMvc.perform(
                        put("/api/user/perfil")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "nome": "Ana Liz"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarPerfilPublico() throws Exception {
        when(perfilService.obterPerfilPublico(eq(2L), isNull()))
                .thenReturn(new PerfilResponseDTO(
                        2L,
                        "joaosilva",
                        "João Silva",
                        null,
                        Role.USER,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(),
                        List.of(),
                        List.of(),
                        0L,
                        0L,
                        0L,
                        0L,
                        false,
                        false
                ));

        mockMvc.perform(get("/api/usuarios/2/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joaosilva"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void deveRetornar404QuandoPerfilPublicoNaoExiste() throws Exception {
        when(perfilService.obterPerfilPublico(eq(99L), isNull()))
                .thenThrow(new UsuarioNaoEncontradoException(99L));

        mockMvc.perform(get("/api/usuarios/99/perfil"))
                .andExpect(status().isNotFound());
    }
}
