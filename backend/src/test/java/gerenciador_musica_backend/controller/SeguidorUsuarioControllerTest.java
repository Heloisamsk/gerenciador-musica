package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.UsuarioSeguidoResumoDTO;
import gerenciador_musica_backend.exception.SeguirUsuarioInvalidoException;
import gerenciador_musica_backend.exception.UsuarioNaoEncontradoException;
import gerenciador_musica_backend.service.SeguidorUsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeguidorUsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class SeguidorUsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeguidorUsuarioService seguidorUsuarioService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveSeguirUsuario() throws Exception {
        doNothing().when(seguidorUsuarioService).seguirUsuario(2L);

        mockMvc.perform(post("/api/usuarios/2/seguidor"))
                .andExpect(status().isNoContent());

        verify(seguidorUsuarioService).seguirUsuario(2L);
    }

    @Test
    void deveDeixarDeSeguirUsuario() throws Exception {
        doNothing().when(seguidorUsuarioService).deixarDeSeguirUsuario(2L);

        mockMvc.perform(delete("/api/usuarios/2/seguidor"))
                .andExpect(status().isNoContent());

        verify(seguidorUsuarioService).deixarDeSeguirUsuario(2L);
    }

    @Test
    void deveListarQuemOUsuarioSegue() throws Exception {
        when(seguidorUsuarioService.listarSeguindo(2L))
                .thenReturn(List.of(new UsuarioSeguidoResumoDTO(3L, "João", "joao")));

        mockMvc.perform(get("/api/usuarios/2/seguindo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("João"));
    }

    @Test
    void deveListarSeguidoresDoUsuario() throws Exception {
        when(seguidorUsuarioService.listarSeguidores(2L))
                .thenReturn(List.of(new UsuarioSeguidoResumoDTO(3L, "João", "joao")));

        mockMvc.perform(get("/api/usuarios/2/seguidores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("joao"));
    }

    @Test
    void deveRetornar400AoSeguirSiMesmo() throws Exception {
        doThrow(new SeguirUsuarioInvalidoException(
                "Não é possível seguir a si mesmo."
        )).when(seguidorUsuarioService).seguirUsuario(2L);

        mockMvc.perform(post("/api/usuarios/2/seguidor"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404AoSeguirUsuarioInexistente() throws Exception {
        doThrow(new UsuarioNaoEncontradoException(99L))
                .when(seguidorUsuarioService).seguirUsuario(99L);

        mockMvc.perform(post("/api/usuarios/99/seguidor"))
                .andExpect(status().isNotFound());
    }
}
