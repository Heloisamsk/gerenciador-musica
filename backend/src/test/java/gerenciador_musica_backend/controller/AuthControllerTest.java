package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.LoginResponseDTO;
import gerenciador_musica_backend.dto.LogoutResponseDTO;
import gerenciador_musica_backend.exception.CredenciaisInvalidasException;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Teste de INTEGRAÇÃO da camada web: sobe apenas o AuthController
 * real, junto com a validação (@Valid) e o GlobalExceptionHandler
 * reais, e simula requisições HTTP como o Postman faria.
 *
 * O AuthService é mockado: não queremos testar a regra de negócio
 * aqui de novo (isso já foi feito no AuthServiceTest), só queremos
 * garantir que o Controller trata corretamente os status HTTP,
 * o JSON de entrada/saída e os erros.
 *
 * addFilters = false desliga os filtros de segurança (JWT) para
 * este teste, já que o foco aqui é o contrato HTTP do Controller,
 * não a autenticação em si. Ainda assim, o Spring tenta instanciar
 * os beans de Filter existentes no projeto (@WebMvcTest também
 * escaneia Filters), então o JwtAuthenticationFilter real também
 * precisa ser substituído por um mock para não exigir suas próprias
 * dependências (JwtService, UsuarioRepository etc.).
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveRetornar200ComTokenQuandoLoginForValido() throws Exception {
        LoginResponseDTO resposta = new LoginResponseDTO();
        resposta.setToken("token-fake");
        resposta.setNome("Maria");
        resposta.setEmail("maria@email.com");
        resposta.setRole(Role.USER);

        when(authService.login(any())).thenReturn(resposta);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "maria@email.com",
                                    "senha": "senha123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-fake"))
                .andExpect(jsonPath("$.email").value("maria@email.com"));
    }

    @Test
    void deveRetornar401QuandoCredenciaisInvalidas() throws Exception {
        when(authService.login(any()))
                .thenThrow(new CredenciaisInvalidasException("E-mail ou senha inválidos."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "maria@email.com",
                                    "senha": "senha-errada"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));
    }

    @Test
    void deveRetornar400QuandoEmailEstaAusente() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "senha": "senha123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    /*
     * O @WebMvcTest sobe só a camada web, sem a configuração completa
     * de segurança (SecurityConfig), então o resolver do
     * @AuthenticationPrincipal não fica disponível aqui. Por isso este
     * caso chama o método do Controller diretamente (sem passar pelo
     * MockMvc/HTTP) para testar o mapeamento "usuário autenticado ->
     * resposta de logout" de forma isolada — ainda um teste de unidade
     * do Controller, só que sem simular a requisição HTTP inteira.
     */
    @Test
    void deveMontarRespostaDeLogoutParaOUsuarioAutenticado() {
        Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", Role.USER);

        when(authService.logout("maria@email.com"))
                .thenReturn(new LogoutResponseDTO("Logout realizado com sucesso para maria@email.com."));

        AuthController controller = new AuthController(authService);

        LogoutResponseDTO resposta = controller.logout(usuario).getBody();

        assertThat(resposta.getMensagem())
                .isEqualTo("Logout realizado com sucesso para maria@email.com.");
    }
}