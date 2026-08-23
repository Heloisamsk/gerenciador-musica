package gerenciador_musica_backend.config;

import gerenciador_musica_backend.controller.AdminAlbumController;
import gerenciador_musica_backend.dto.AlbumAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.AlbumService;
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

@WebMvcTest(AdminAlbumController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
@TestPropertySource(properties =
        "app.cors.allowed-origins=http://localhost:4200"
)
class AdminAlbumEndpointSecurityTest {

    private static final String REQUEST_CADASTRO = """
            {
                "titulo": "A Night at the Opera",
                "idArtista": 1,
                "anoLancamento": 1975,
                "capaUrl": null
            }
            """;

    private static final String REQUEST_ATUALIZACAO = """
            {
                "titulo": "A Night at the Opera",
                "anoLancamento": 1975,
                "capaUrl": null
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRetornar401AoCadastrarSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/admin/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_CADASTRO))
                .andExpect(status().isUnauthorized());

        verify(albumService, never()).cadastrarAlbum(
                any(AlbumRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403AoCadastrarComoUsuarioComum()
            throws Exception {
        mockMvc.perform(post("/api/admin/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_CADASTRO))
                .andExpect(status().isForbidden());

        verify(albumService, never()).cadastrarAlbum(
                any(AlbumRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirCadastroParaAdministrador() throws Exception {
        when(albumService.cadastrarAlbum(
                any(AlbumRequestDTO.class)
        )).thenReturn(montarResposta());

        mockMvc.perform(post("/api/admin/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_CADASTRO))
                .andExpect(status().isCreated());

        verify(albumService).cadastrarAlbum(
                any(AlbumRequestDTO.class)
        );
    }

    @Test
    void deveRetornar401AoAtualizarSemAutenticacao() throws Exception {
        mockMvc.perform(put("/api/admin/albuns/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isUnauthorized());

        verify(albumService, never()).atualizarAlbum(
                eq(10L),
                any(AlbumAtualizacaoRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403AoAtualizarComoUsuarioComum()
            throws Exception {
        mockMvc.perform(put("/api/admin/albuns/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isForbidden());

        verify(albumService, never()).atualizarAlbum(
                eq(10L),
                any(AlbumAtualizacaoRequestDTO.class)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAtualizacaoParaAdministrador() throws Exception {
        when(albumService.atualizarAlbum(
                eq(10L),
                any(AlbumAtualizacaoRequestDTO.class)
        )).thenReturn(montarResposta());

        mockMvc.perform(put("/api/admin/albuns/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isOk());

        verify(albumService).atualizarAlbum(
                eq(10L),
                any(AlbumAtualizacaoRequestDTO.class)
        );
    }

    @Test
    void deveRetornar401AoExcluirSemAutenticacao() throws Exception {
        mockMvc.perform(delete("/api/admin/albuns/10"))
                .andExpect(status().isUnauthorized());

        verify(albumService, never()).excluirAlbum(10L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRetornar403AoExcluirComoUsuarioComum() throws Exception {
        mockMvc.perform(delete("/api/admin/albuns/10"))
                .andExpect(status().isForbidden());

        verify(albumService, never()).excluirAlbum(10L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirExclusaoParaAdministrador() throws Exception {
        mockMvc.perform(delete("/api/admin/albuns/10"))
                .andExpect(status().isNoContent());

        verify(albumService).excluirAlbum(10L);
    }

    private AlbumResponseDTO montarResposta() {
        return new AlbumResponseDTO(
                10L,
                "A Night at the Opera",
                (short) 1975,
                null,
                new ArtistaResumoDTO(
                        1L,
                        "Queen",
                        "Queen",
                        "Banda britânica de rock.",
                        null
                )
        );
    }
}
