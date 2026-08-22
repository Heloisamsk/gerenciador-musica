package gerenciador_musica_backend.config;

import gerenciador_musica_backend.controller.AdminArtistaController;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.ArtistaService;
import gerenciador_musica_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminArtistaController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
@TestPropertySource(properties =
        "app.cors.allowed-origins=http://localhost:4200"
)
class AdminArtistaEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistaService artistaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(delete("/api/admin/artistas/1"))
                .andExpect(status().isUnauthorized());

        verify(artistaService, never()).excluirArtista(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403ParaUsuarioComum() throws Exception {
        mockMvc.perform(delete("/api/admin/artistas/1"))
                .andExpect(status().isForbidden());

        verify(artistaService, never()).excluirArtista(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirExclusaoParaAdministrador() throws Exception {
        mockMvc.perform(delete("/api/admin/artistas/1"))
                .andExpect(status().isNoContent());

        verify(artistaService).excluirArtista(1L);
    }
}
