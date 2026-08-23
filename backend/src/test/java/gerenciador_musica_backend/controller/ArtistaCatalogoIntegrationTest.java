package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaRequestDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.dto.MusicaRequestDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;
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
@Transactional
class ArtistaCatalogoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String tokenAdmin;

    @BeforeEach
    void criarAdministrador() {
        String sufixo = sufixoUnico();
        Usuario administrador = usuarioRepository.saveAndFlush(
                new Usuario(
                        "Admin Catálogo " + sufixo,
                        "admin-catalogo-" + sufixo + "@teste.com",
                        passwordEncoder.encode("Senha@123"),
                        Role.ADMIN
                )
        );

        tokenAdmin = jwtService.gerarToken(administrador);
    }

    @Test
    void deveValidarCrudDeArtistaIntegradoAoCatalogo() throws Exception {
        String sufixo = sufixoUnico();
        ArtistaResponseDTO artista = cadastrarArtista(
                new ArtistaRequestDTO(
                        "Artista integrado " + sufixo,
                        "Nome completo original",
                        "Descrição original.",
                        "https://exemplo.com/foto-original.jpg"
                )
        );

        assertThat(buscarArtista(artista.idArtista()))
                .isEqualTo(artista);

        AlbumResponseDTO album = cadastrarAlbum(
                new AlbumRequestDTO(
                        "Álbum integrado " + sufixo,
                        artista.idArtista(),
                        (short) 2026,
                        "https://exemplo.com/capa.jpg"
                )
        );
        MusicaResponseDTO musica = cadastrarMusica(
                new MusicaRequestDTO(
                        "Música integrada " + sufixo,
                        null,
                        215,
                        (short) 2026,
                        artista.idArtista(),
                        Set.of(),
                        album.idAlbum(),
                        Set.of("Gênero integração " + sufixo)
                )
        );

        ArtistaResponseDTO artistaAtualizado = atualizarArtista(
                artista.idArtista(),
                new ArtistaRequestDTO(
                        "Artista atualizado " + sufixo,
                        "Nome completo atualizado",
                        "Descrição atualizada no catálogo.",
                        "https://exemplo.com/foto-atualizada.jpg"
                )
        );

        ArtistaResponseDTO dadosAtualizadosEsperados =
                new ArtistaResponseDTO(
                        artista.idArtista(),
                        "Artista atualizado " + sufixo,
                        "Nome completo atualizado",
                        "Descrição atualizada no catálogo.",
                        "https://exemplo.com/foto-atualizada.jpg"
                );

        assertThat(artistaAtualizado)
                .isEqualTo(dadosAtualizadosEsperados);
        assertThat(buscarArtista(artista.idArtista()))
                .isEqualTo(artistaAtualizado);
        assertThat(listarArtistas())
                .filteredOn(item -> item.idArtista()
                        .equals(artista.idArtista()))
                .containsExactly(artistaAtualizado);

        validarAssociacoesPreservadas(
                artistaAtualizado,
                album,
                musica
        );

        mockMvc.perform(delete(
                        "/api/admin/artistas/{id}",
                        artista.idArtista()
                )
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Não é possível excluir o artista porque "
                                + "ele possui álbuns associados."
                ));

        assertThat(buscarAlbum(album.idAlbum()).idAlbum())
                .isEqualTo(album.idAlbum());
        assertThat(buscarMusica(musica.id()).id())
                .isEqualTo(musica.id());

        ArtistaResponseDTO artistaLivre = cadastrarArtista(
                new ArtistaRequestDTO(
                        "Artista livre " + sufixo,
                        "Artista livre completo",
                        "Artista criado para validar a exclusão.",
                        null
                )
        );

        mockMvc.perform(delete(
                        "/api/admin/artistas/{id}",
                        artistaLivre.idArtista()
                )
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get(
                        "/api/artistas/{id}",
                        artistaLivre.idArtista()
                )
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isNotFound());
    }

    private void validarAssociacoesPreservadas(
            ArtistaResponseDTO artistaAtualizado,
            AlbumResponseDTO album,
            MusicaResponseDTO musica
    ) throws Exception {
        AlbumResponseDTO albumDepoisDaEdicao = buscarAlbum(
                album.idAlbum()
        );
        assertThat(albumDepoisDaEdicao.artista().id())
                .isEqualTo(artistaAtualizado.idArtista());
        assertThat(albumDepoisDaEdicao.artista().nome())
                .isEqualTo(artistaAtualizado.nome());

        MusicaResponseDTO musicaDepoisDaEdicao = buscarMusica(
                musica.id()
        );
        assertThat(musicaDepoisDaEdicao.artistaPrincipal().id())
                .isEqualTo(artistaAtualizado.idArtista());
        assertThat(musicaDepoisDaEdicao.artistaPrincipal().nome())
                .isEqualTo(artistaAtualizado.nome());
        assertThat(musicaDepoisDaEdicao.album().id())
                .isEqualTo(album.idAlbum());
    }

    private ArtistaResponseDTO cadastrarArtista(
            ArtistaRequestDTO request
    ) throws Exception {
        String responseJson = mockMvc.perform(
                        post("/api/admin/artistas")
                                .header(
                                        "Authorization",
                                        tokenAutorizacao()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        request
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                responseJson,
                ArtistaResponseDTO.class
        );
    }

    private AlbumResponseDTO cadastrarAlbum(
            AlbumRequestDTO request
    ) throws Exception {
        String responseJson = mockMvc.perform(
                        post("/api/admin/albuns")
                                .header(
                                        "Authorization",
                                        tokenAutorizacao()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        request
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                responseJson,
                AlbumResponseDTO.class
        );
    }

    private MusicaResponseDTO cadastrarMusica(
            MusicaRequestDTO request
    ) throws Exception {
        String responseJson = mockMvc.perform(
                        post("/api/admin/musicas")
                                .header(
                                        "Authorization",
                                        tokenAutorizacao()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        request
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                responseJson,
                MusicaResponseDTO.class
        );
    }

    private ArtistaResponseDTO atualizarArtista(
            Long idArtista,
            ArtistaRequestDTO request
    ) throws Exception {
        String responseJson = mockMvc.perform(
                        put("/api/admin/artistas/{id}", idArtista)
                                .header(
                                        "Authorization",
                                        tokenAutorizacao()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        request
                                ))
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

    private ArtistaResponseDTO buscarArtista(Long idArtista)
            throws Exception {
        String responseJson = buscarJson(
                "/api/artistas/{id}",
                idArtista
        );

        return objectMapper.readValue(
                responseJson,
                ArtistaResponseDTO.class
        );
    }

    private AlbumResponseDTO buscarAlbum(Long idAlbum) throws Exception {
        String responseJson = buscarJson(
                "/api/albuns/{id}",
                idAlbum
        );

        return objectMapper.readValue(
                responseJson,
                AlbumResponseDTO.class
        );
    }

    private MusicaResponseDTO buscarMusica(Long idMusica)
            throws Exception {
        String responseJson = buscarJson(
                "/api/musicas/{id}",
                idMusica
        );

        return objectMapper.readValue(
                responseJson,
                MusicaResponseDTO.class
        );
    }

    private String buscarJson(String endpoint, Long id) throws Exception {
        return mockMvc.perform(get(endpoint, id)
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
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

    private String tokenAutorizacao() {
        return "Bearer " + tokenAdmin;
    }

    private String sufixoUnico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
