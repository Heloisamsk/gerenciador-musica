package gerenciador_musica_backend.config;

import gerenciador_musica_backend.controller.AdminRelatorioController;
import gerenciador_musica_backend.dto.RelatorioCatalogoDTO;
import gerenciador_musica_backend.dto.ResumoCatalogoDTO;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.JwtService;
import gerenciador_musica_backend.service.RelatorioCsvService;
import gerenciador_musica_backend.service.RelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRelatorioController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
@TestPropertySource(properties =
        "app.cors.allowed-origins=http://localhost:4200"
)
class AdminRelatorioEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RelatorioService relatorioService;

    @MockitoBean
    private RelatorioCsvService relatorioCsvService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/catalogo"))
                .andExpect(status().isUnauthorized());

        verify(relatorioService, never()).gerarRelatorioCatalogo();
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403ParaUsuarioComum() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/catalogo"))
                .andExpect(status().isForbidden());

        verify(relatorioService, never()).gerarRelatorioCatalogo();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirRelatorioParaAdministrador() throws Exception {
        when(relatorioService.gerarRelatorioCatalogo())
                .thenReturn(montarRelatorio());

        mockMvc.perform(get("/api/admin/relatorios/catalogo"))
                .andExpect(status().isOk());

        verify(relatorioService).gerarRelatorioCatalogo();
    }

    private RelatorioCatalogoDTO montarRelatorio() {
        return new RelatorioCatalogoDTO(
                OffsetDateTime.parse("2026-08-28T12:00:00Z"),
                new ResumoCatalogoDTO(0, 0, 0, 0, 0),
                List.of(),
                List.of()
        );
    }
}
