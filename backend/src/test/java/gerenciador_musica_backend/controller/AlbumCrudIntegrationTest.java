package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.AlbumAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.MusicaRequestDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.ArtistaRepository;
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
import tools.jackson.databind.ObjectMapper;

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
class AlbumCrudIntegrationTest {

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

    private String tokenAdmin;

    @BeforeEach
    void criarAdministrador() {
        String sufixo = sufixoUnico();
        Usuario administrador = usuarioRepository.saveAndFlush(
                new Usuario(
                        "Admin CRUD Álbum " + sufixo,
                        "admin-album-" + sufixo + "@teste.com",
                        passwordEncoder.encode("Senha@123"),
                        Role.ADMIN
                )
        );

        tokenAdmin = jwtService.gerarToken(administrador);
    }

    @Test
    void deveExecutarPostGetPutGetDelete() throws Exception {
        String sufixo = sufixoUnico();
        Artista artista = salvarArtista("Artista CRUD " + sufixo);
        AlbumResponseDTO cadastrado = cadastrarAlbum(
                new AlbumRequestDTO(
                        "Álbum CRUD " + sufixo,
                        artista.getIdArtista(),
                        (short) 2025,
                        "https://example.com/capa-original.jpg"
                )
        );

        assertThat(cadastrado.idAlbum()).isNotNull();
        assertThat(buscarAlbum(cadastrado.idAlbum()))
                .isEqualTo(cadastrado);

        AlbumResponseDTO atualizado = atualizarAlbum(
                cadastrado.idAlbum(),
                new AlbumAtualizacaoRequestDTO(
                        "  Álbum   atualizado " + sufixo + "  ",
                        (short) 2026,
                        "   "
                )
        );

        assertThat(atualizado).isEqualTo(new AlbumResponseDTO(
                cadastrado.idAlbum(),
                "Álbum atualizado " + sufixo,
                (short) 2026,
                null,
                cadastrado.artista()
        ));
        assertThat(buscarAlbum(cadastrado.idAlbum()))
                .isEqualTo(atualizado);

        excluirAlbum(cadastrado.idAlbum());
        verificarAlbumInexistente(cadastrado.idAlbum());
        assertThat(artistaRepository.existsById(
                artista.getIdArtista()
        )).isTrue();
    }

    @Test
    void devePreservarMusicaEBloquearExclusaoDoAlbum()
            throws Exception {
        String sufixo = sufixoUnico();
        Artista artista = salvarArtista("Artista associado " + sufixo);
        AlbumResponseDTO album = cadastrarAlbum(
                new AlbumRequestDTO(
                        "Álbum associado " + sufixo,
                        artista.getIdArtista(),
                        (short) 2025,
                        null
                )
        );
        MusicaResponseDTO musica = cadastrarMusica(
                new MusicaRequestDTO(
                        "Música associada " + sufixo,
                        null,
                        180,
                        (short) 2025,
                        artista.getIdArtista(),
                        Set.of(),
                        album.idAlbum(),
                        Set.of("Gênero integração " + sufixo)
                )
        );

        AlbumResponseDTO atualizado = atualizarAlbum(
                album.idAlbum(),
                new AlbumAtualizacaoRequestDTO(
                        "Álbum associado atualizado " + sufixo,
                        (short) 2026,
                        "https://example.com/capa-atualizada.jpg"
                )
        );
        MusicaResponseDTO musicaDepoisDaEdicao = buscarMusica(
                musica.id()
        );

        assertThat(musicaDepoisDaEdicao.album().id())
                .isEqualTo(album.idAlbum());
        assertThat(musicaDepoisDaEdicao.album().titulo())
                .isEqualTo(atualizado.titulo());

        mockMvc.perform(delete(
                        "/api/admin/albuns/{id}",
                        album.idAlbum()
                )
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Não é possível excluir o álbum porque "
                                + "ele possui músicas associadas."
                ));

        assertThat(buscarAlbum(album.idAlbum()).idAlbum())
                .isEqualTo(album.idAlbum());
        assertThat(buscarMusica(musica.id()).album().id())
                .isEqualTo(album.idAlbum());
        assertThat(artistaRepository.existsById(
                artista.getIdArtista()
        )).isTrue();
    }

    private AlbumResponseDTO cadastrarAlbum(
            AlbumRequestDTO request
    ) throws Exception {
        var resultado = mockMvc.perform(post("/api/admin/albuns")
                        .header("Authorization", tokenAutorizacao())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        AlbumResponseDTO response = objectMapper.readValue(
                resultado.getResponse().getContentAsString(),
                AlbumResponseDTO.class
        );

        assertThat(resultado.getResponse().getHeader("Location"))
                .isEqualTo(
                        "http://localhost/api/albuns/"
                                + response.idAlbum()
                );

        return response;
    }

    private AlbumResponseDTO buscarAlbum(Long idAlbum)
            throws Exception {
        String responseJson = mockMvc.perform(get(
                        "/api/albuns/{id}",
                        idAlbum
                )
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                responseJson,
                AlbumResponseDTO.class
        );
    }

    private AlbumResponseDTO atualizarAlbum(
            Long idAlbum,
            AlbumAtualizacaoRequestDTO request
    ) throws Exception {
        String responseJson = mockMvc.perform(put(
                        "/api/admin/albuns/{id}",
                        idAlbum
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
                AlbumResponseDTO.class
        );
    }

    private void excluirAlbum(Long idAlbum) throws Exception {
        mockMvc.perform(delete(
                        "/api/admin/albuns/{id}",
                        idAlbum
                )
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    private void verificarAlbumInexistente(Long idAlbum)
            throws Exception {
        mockMvc.perform(get("/api/albuns/{id}", idAlbum)
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Álbum não encontrado com o ID: " + idAlbum
                ));
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

    private MusicaResponseDTO buscarMusica(Long idMusica)
            throws Exception {
        String responseJson = mockMvc.perform(get(
                        "/api/musicas/{id}",
                        idMusica
                )
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                responseJson,
                MusicaResponseDTO.class
        );
    }

    private Artista salvarArtista(String nome) {
        return artistaRepository.saveAndFlush(
                new Artista(
                        nome,
                        nome + " completo",
                        "Descrição de teste.",
                        null
                )
        );
    }

    private String tokenAutorizacao() {
        return "Bearer " + tokenAdmin;
    }

    private String sufixoUnico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
