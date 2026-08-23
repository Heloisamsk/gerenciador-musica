package gerenciador_musica_backend.config;

import gerenciador_musica_backend.controller.AdminMusicaController;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.dto.MusicaRequestDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.JwtService;
import gerenciador_musica_backend.service.MusicaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMusicaController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
@TestPropertySource(properties =
        "app.cors.allowed-origins=http://localhost:4200"
)
class AdminMusicaEndpointSecurityTest {

    private static final String REQUEST_ATUALIZACAO = """
            {
                "titulo": "Bohemian Rhapsody",
                "letra": null,
                "duracaoSegundos": 354,
                "anoLancamento": 1975,
                "artistaPrincipalId": 1,
                "artistasParticipantesIds": [],
                "albumId": 1,
                "generos": ["Rock"]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MusicaService musicaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRetornar401AoAtualizarSemAutenticacao() throws Exception {
        mockMvc.perform(put("/api/admin/musicas/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isUnauthorized());

        verify(musicaService, never()).atualizarMusica(
                eq(10L),
                any(MusicaRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403AoAtualizarComoUsuarioComum()
            throws Exception {
        mockMvc.perform(put("/api/admin/musicas/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isForbidden());

        verify(musicaService, never()).atualizarMusica(
                eq(10L),
                any(MusicaRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAtualizacaoParaAdministrador() throws Exception {
        when(musicaService.atualizarMusica(
                eq(10L),
                any(MusicaRequestDTO.class)
        )).thenReturn(montarResposta());

        mockMvc.perform(put("/api/admin/musicas/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isOk());

        verify(musicaService).atualizarMusica(
                eq(10L),
                any(MusicaRequestDTO.class)
        );
    }

    @Test
    void deveRetornar401AoExcluirSemAutenticacao() throws Exception {
        mockMvc.perform(delete("/api/admin/musicas/10"))
                .andExpect(status().isUnauthorized());

        verify(musicaService, never()).excluirMusica(10L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403AoExcluirComoUsuarioComum() throws Exception {
        mockMvc.perform(delete("/api/admin/musicas/10"))
                .andExpect(status().isForbidden());

        verify(musicaService, never()).excluirMusica(10L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirExclusaoParaAdministrador() throws Exception {
        mockMvc.perform(delete("/api/admin/musicas/10"))
                .andExpect(status().isNoContent());

        verify(musicaService).excluirMusica(10L);
    }

    private MusicaResponseDTO montarResposta() {
        return new MusicaResponseDTO(
                10L,
                "Bohemian Rhapsody",
                null,
                354,
                (short) 1975,
                new ArtistaResumoDTO(
                        1L,
                        "Queen",
                        "Queen",
                        "Banda britânica de rock.",
                        null
                ),
                null,
                Set.of(),
                Set.of()
        );
    }
}
