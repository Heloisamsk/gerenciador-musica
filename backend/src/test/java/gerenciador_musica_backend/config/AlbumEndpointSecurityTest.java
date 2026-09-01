package gerenciador_musica_backend.config;

import gerenciador_musica_backend.controller.AlbumController;
import gerenciador_musica_backend.dto.AlbumCatalogoDTO;
import gerenciador_musica_backend.dto.AlbumDetalheDTO;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.AlbumService;
import gerenciador_musica_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlbumController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
@TestPropertySource(properties =
        "app.cors.allowed-origins=http://localhost:4200"
)
class AlbumEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveProtegerDetalhesDoAlbumSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/albuns/10/detalhes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void devePermitirDetalhesDoAlbumParaUsuarioAutenticado()
            throws Exception {
        when(albumService.buscarDetalhesCatalogo(10L))
                .thenReturn(new AlbumDetalheDTO(
                        new AlbumCatalogoDTO(
                                10L,
                                1L,
                                "Queen",
                                "A Night at the Opera",
                                (short) 1975,
                                null,
                                0L,
                                0L,
                                false
                        ),
                        List.of(),
                        List.of()
                ));

        mockMvc.perform(get("/api/albuns/10/detalhes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.album.idAlbum").value(10));
    }
}
