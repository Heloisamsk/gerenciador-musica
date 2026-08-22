package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.ArtistaRequestDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ArtistaCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Usuario usuarioAdmin;
    private String tokenAdmin;
    private Long idArtistaCriado;

    @BeforeEach
    void criarAdministrador() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);

        usuarioAdmin = usuarioRepository.saveAndFlush(new Usuario(
                "Admin CRUD Artista " + sufixo,
                "admin-artista-" + sufixo + "@teste.com",
                passwordEncoder.encode("Senha@123"),
                Role.ADMIN
        ));
        tokenAdmin = jwtService.gerarToken(usuarioAdmin);
    }

    @AfterEach
    void limparDadosCriados() {
        if (idArtistaCriado != null) {
            artistaRepository
                    .findById(idArtistaCriado)
                    .ifPresent(artistaRepository::delete);
            artistaRepository.flush();
        }

        if (usuarioAdmin != null) {
            usuarioRepository.delete(usuarioAdmin);
            usuarioRepository.flush();
        }
    }

    @Test
    void deveExecutarFluxoCompletoDoCrudDeArtista() throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        ArtistaRequestDTO requestCadastro = new ArtistaRequestDTO(
                "Artista CRUD " + sufixo,
                "Nome completo original",
                "Descrição original.",
                "https://exemplo.com/foto-original.jpg"
        );
        ArtistaResponseDTO artistaCadastrado = cadastrarArtista(
                requestCadastro
        );

        assertThat(artistaCadastrado.idArtista()).isNotNull();
        assertThat(buscarArtista(artistaCadastrado.idArtista()))
                .isEqualTo(artistaCadastrado);

        ArtistaRequestDTO requestAtualizacao = new ArtistaRequestDTO(
                "  Artista atualizado " + sufixo + "  ",
                "  Nome completo atualizado  ",
                "  Descrição atualizada.  ",
                "   "
        );
        ArtistaResponseDTO artistaAtualizado = atualizarArtista(
                artistaCadastrado.idArtista(),
                requestAtualizacao
        );

        assertThat(artistaAtualizado).isEqualTo(new ArtistaResponseDTO(
                artistaCadastrado.idArtista(),
                "Artista atualizado " + sufixo,
                "Nome completo atualizado",
                "Descrição atualizada.",
                null
        ));
        assertThat(buscarArtista(artistaCadastrado.idArtista()))
                .isEqualTo(artistaAtualizado);
        assertThat(listarArtistas())
                .filteredOn(artista -> artista.idArtista()
                        .equals(artistaCadastrado.idArtista()))
                .containsExactly(artistaAtualizado);

        excluirArtista(artistaCadastrado.idArtista());
        verificarArtistaInexistente(artistaCadastrado.idArtista());
        assertThat(listarArtistas())
                .extracting(ArtistaResponseDTO::idArtista)
                .doesNotContain(artistaCadastrado.idArtista());
    }

    private ArtistaResponseDTO cadastrarArtista(
            ArtistaRequestDTO request
    ) throws Exception {
        var resultado = mockMvc.perform(post("/api/admin/artistas")
                        .header("Authorization", tokenAutorizacao())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        ArtistaResponseDTO response = objectMapper.readValue(
                resultado.getResponse().getContentAsString(),
                ArtistaResponseDTO.class
        );
        idArtistaCriado = response.idArtista();

        assertThat(resultado.getResponse().getHeader("Location"))
                .isEqualTo(
                        "http://localhost/api/artistas/"
                                + idArtistaCriado
                );

        return response;
    }

    private ArtistaResponseDTO buscarArtista(Long idArtista)
            throws Exception {
        String responseJson = mockMvc.perform(
                        get("/api/artistas/{id}", idArtista)
                                .header(
                                        "Authorization",
                                        tokenAutorizacao()
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                responseJson,
                ArtistaResponseDTO.class
        );
    }

    private ArtistaResponseDTO atualizarArtista(
            Long idArtista,
            ArtistaRequestDTO request
    ) throws Exception {
        String responseJson = mockMvc.perform(put(
                        "/api/admin/artistas/{id}",
                        idArtista
                )
                        .header("Authorization", tokenAutorizacao())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                responseJson,
                ArtistaResponseDTO.class
        );
    }

    private List<ArtistaResponseDTO> listarArtistas() throws Exception {
        String responseJson = mockMvc.perform(get("/api/artistas")
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                responseJson,
                new TypeReference<List<ArtistaResponseDTO>>() {
                }
        );
    }

    private void excluirArtista(Long idArtista) throws Exception {
        mockMvc.perform(delete(
                        "/api/admin/artistas/{id}",
                        idArtista
                )
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    private void verificarArtistaInexistente(Long idArtista)
            throws Exception {
        mockMvc.perform(get("/api/artistas/{id}", idArtista)
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Artista não encontrado com o ID: " + idArtista
                ));
    }

    private String tokenAutorizacao() {
        return "Bearer " + tokenAdmin;
    }
}
