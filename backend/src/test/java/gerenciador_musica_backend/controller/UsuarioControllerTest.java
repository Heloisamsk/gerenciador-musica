package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.UsuarioResponseDTO;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveCadastrarComoUserMesmoQuandoJsonContemRoleAdmin()
            throws Exception {

        UsuarioResponseDTO resposta =
                new UsuarioResponseDTO();

        resposta.setId(1L);
        resposta.setNome("Maria");
        resposta.setEmail("maria@email.com");
        resposta.setRole(Role.USER);

        when(usuarioService.cadastrarUsuario(
                argThat(request ->
                        request != null
                                && "Maria".equals(request.getNome())
                                && "maria@email.com"
                                .equals(request.getEmail())
                                && "senha123"
                                .equals(request.getSenha())
                )
        )).thenReturn(resposta);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                            "nome": "Maria",
                                            "email": "maria@email.com",
                                            "senha": "senha123",
                                            "role": "ADMIN"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.role")
                                .value("USER")
                );

        verify(usuarioService).cadastrarUsuario(
                argThat(request ->
                        "maria@email.com"
                                .equals(request.getEmail())
                )
        );
    }

    @Test
    void deveRetornar400QuandoSenhaPossuiMenosDeSeisCaracteres()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                            "nome": "Maria",
                                            "email": "maria@email.com",
                                            "senha": "12345"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.fieldErrors.senha")
                                .exists()
                );
    }
}