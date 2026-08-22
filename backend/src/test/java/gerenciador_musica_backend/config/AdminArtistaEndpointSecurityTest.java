package gerenciador_musica_backend.config;

import gerenciador_musica_backend.controller.AdminArtistaController;
import gerenciador_musica_backend.dto.ArtistaRequestDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.ArtistaService;
import gerenciador_musica_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private static final String REQUEST_ATUALIZACAO = """
            {
                "nome": "Queen Atualizado",
                "nomeCompleto": "Queen",
                "descricao": "Descrição atualizada.",
                "fotoPerfilUrl": null
            }
            """;

    @Test
    void deveRetornar401AoCadastrarSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/admin/artistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isUnauthorized());

        verify(artistaService, never()).cadastrarArtista(
                any(ArtistaRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403AoCadastrarComoUsuarioComum()
            throws Exception {
        mockMvc.perform(post("/api/admin/artistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isForbidden());

        verify(artistaService, never()).cadastrarArtista(
                any(ArtistaRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirCadastroParaAdministrador() throws Exception {
        when(artistaService.cadastrarArtista(
                any(ArtistaRequestDTO.class)
        )).thenReturn(new ArtistaResponseDTO(
                1L,
                "Queen Atualizado",
                "Queen",
                "Descrição atualizada.",
                null
        ));

        mockMvc.perform(post("/api/admin/artistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isCreated());

        verify(artistaService).cadastrarArtista(
                any(ArtistaRequestDTO.class)
        );
    }

    @Test
    void deveRetornar401AoAtualizarSemAutenticacao() throws Exception {
        mockMvc.perform(put("/api/admin/artistas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isUnauthorized());

        verify(artistaService, never()).atualizarArtista(
                eq(1L),
                any(ArtistaRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403AoAtualizarComoUsuarioComum()
            throws Exception {
        mockMvc.perform(put("/api/admin/artistas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isForbidden());

        verify(artistaService, never()).atualizarArtista(
                eq(1L),
                any(ArtistaRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAtualizacaoParaAdministrador() throws Exception {
        when(artistaService.atualizarArtista(
                eq(1L),
                any(ArtistaRequestDTO.class)
        )).thenReturn(new ArtistaResponseDTO(
                1L,
                "Queen Atualizado",
                "Queen",
                "Descrição atualizada.",
                null
        ));

        mockMvc.perform(put("/api/admin/artistas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isOk());

        verify(artistaService).atualizarArtista(
                eq(1L),
                any(ArtistaRequestDTO.class)
        );
    }

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
