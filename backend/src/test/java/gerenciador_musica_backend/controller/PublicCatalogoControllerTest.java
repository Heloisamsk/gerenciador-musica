package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.RestAccessDeniedHandler;
import gerenciador_musica_backend.config.RestAuthenticationEntryPoint;
import gerenciador_musica_backend.config.SecurityConfig;
import gerenciador_musica_backend.dto.AlbumCapaPublicaDTO;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.AlbumService;
import gerenciador_musica_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Usa a cadeia real de segurança (sem addFilters = false) para provar
 * que o endpoint fica acessível sem token, e não apenas que o
 * controller responde corretamente quando a segurança é ignorada.
 */
@WebMvcTest(PublicCatalogoController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
@TestPropertySource(properties =
        "app.cors.allowed-origins=http://localhost:4200"
)
class PublicCatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveListarCapasPublicasSemAutenticacao() throws Exception {
        when(albumService.listarCapasPublicas())
                .thenReturn(List.of(
                        new AlbumCapaPublicaDTO(
                                10L,
                                "A Night at the Opera",
                                "https://exemplo.com/capas/10.jpg"
                        )
                ));

        mockMvc.perform(get("/api/public/albuns/capas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].titulo")
                        .value("A Night at the Opera"))
                .andExpect(jsonPath("$[0].capaUrl")
                        .value("https://exemplo.com/capas/10.jpg"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverCapas() throws Exception {
        when(albumService.listarCapasPublicas())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/public/albuns/capas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
