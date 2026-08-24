package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.MusicaRequestDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.JwtService;
import jakarta.persistence.EntityManager;
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
class MusicaCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EntityManager entityManager;

    private String tokenAdmin;

    @BeforeEach
    void criarAdministrador() {
        String sufixo = sufixoUnico();
        Usuario administrador = usuarioRepository.saveAndFlush(
                new Usuario(
                        "Admin CRUD Música " + sufixo,
                        "admin-musica-" + sufixo + "@teste.com",
                        passwordEncoder.encode("Senha@123"),
                        Role.ADMIN
                )
        );

        tokenAdmin = jwtService.gerarToken(administrador);
    }

    @Test
    void deveExecutarPostGetPutGetDeleteEGet404() throws Exception {
        String sufixo = sufixoUnico();
        Artista artistaInicial = salvarArtista(
                "Artista inicial " + sufixo
        );
        Artista artistaAtualizado = salvarArtista(
                "Artista atualizado " + sufixo
        );
        Artista participante = salvarArtista(
                "Participante " + sufixo
        );
        Album albumAtualizado = albumRepository.saveAndFlush(
                new Album(
                        artistaAtualizado,
                        "Álbum CRUD " + sufixo,
                        (short) 2026,
                        null
                )
        );

        MusicaResponseDTO cadastrada = cadastrarMusica(
                new MusicaRequestDTO(
                        "Música CRUD " + sufixo,
                        "Texto inicial de teste.",
                        180,
                        (short) 2024,
                        artistaInicial.getIdArtista(),
                        Set.of(),
                        null,
                        Set.of("Gênero inicial " + sufixo),
                        "https://www.youtube.com/watch?v=M7lc1UVf-VE"
                )
        );

        assertThat(cadastrada.id()).isNotNull();
        assertThat(cadastrada.youtubeVideoId())
                .isEqualTo("M7lc1UVf-VE");

        entityManager.flush();
        entityManager.clear();

        assertThat(buscarMusica(cadastrada.id()))
                .isEqualTo(cadastrada);

        MusicaResponseDTO atualizada = atualizarMusica(
                cadastrada.id(),
                new MusicaRequestDTO(
                        "  Música   atualizada " + sufixo + "  ",
                        "  Texto atualizado de teste.  ",
                        240,
                        (short) 2026,
                        artistaAtualizado.getIdArtista(),
                        Set.of(participante.getIdArtista()),
                        albumAtualizado.getIdAlbum(),
                        Set.of("  Gênero atualizado " + sufixo + "  "),
                        "https://youtube.com/shorts/dQw4w9WgXcQ"
                )
        );

        assertThat(atualizada.id()).isEqualTo(cadastrada.id());
        assertThat(atualizada.titulo())
                .isEqualTo("Música atualizada " + sufixo);
        assertThat(atualizada.letra())
                .isEqualTo("Texto atualizado de teste.");
        assertThat(atualizada.duracaoSegundos()).isEqualTo(240);
        assertThat(atualizada.anoLancamento()).isEqualTo((short) 2026);
        assertThat(atualizada.artistaPrincipal().id())
                .isEqualTo(artistaAtualizado.getIdArtista());
        assertThat(atualizada.album().id())
                .isEqualTo(albumAtualizado.getIdAlbum());
        assertThat(atualizada.artistasParticipantes())
                .extracting(artista -> artista.id())
                .containsExactly(participante.getIdArtista());
        assertThat(atualizada.generos())
                .extracting(genero -> genero.nome())
                .containsExactly("Gênero atualizado " + sufixo);
        assertThat(atualizada.youtubeVideoId())
                .isEqualTo("dQw4w9WgXcQ");

        entityManager.flush();
        entityManager.clear();

        assertThat(buscarMusica(cadastrada.id()))
                .isEqualTo(atualizada);

        excluirMusica(cadastrada.id());
        entityManager.flush();
        entityManager.clear();

        verificarMusicaInexistente(cadastrada.id());
        assertThat(artistaRepository.existsById(
                artistaInicial.getIdArtista()
        )).isTrue();
        assertThat(artistaRepository.existsById(
                artistaAtualizado.getIdArtista()
        )).isTrue();
        assertThat(artistaRepository.existsById(
                participante.getIdArtista()
        )).isTrue();
        assertThat(albumRepository.existsById(
                albumAtualizado.getIdAlbum()
        )).isTrue();
    }

    private MusicaResponseDTO cadastrarMusica(
            MusicaRequestDTO request
    ) throws Exception {
        var resultado = mockMvc.perform(post("/api/admin/musicas")
                        .header("Authorization", tokenAutorizacao())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        MusicaResponseDTO response = objectMapper.readValue(
                resultado.getResponse().getContentAsString(),
                MusicaResponseDTO.class
        );

        assertThat(resultado.getResponse().getHeader("Location"))
                .isEqualTo(
                        "http://localhost/api/musicas/" + response.id()
                );

        return response;
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

    private MusicaResponseDTO atualizarMusica(
            Long idMusica,
            MusicaRequestDTO request
    ) throws Exception {
        String responseJson = mockMvc.perform(put(
                        "/api/admin/musicas/{id}",
                        idMusica
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
                MusicaResponseDTO.class
        );
    }

    private void excluirMusica(Long idMusica) throws Exception {
        mockMvc.perform(delete(
                        "/api/admin/musicas/{id}",
                        idMusica
                )
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    private void verificarMusicaInexistente(Long idMusica)
            throws Exception {
        mockMvc.perform(get("/api/musicas/{id}", idMusica)
                        .header("Authorization", tokenAutorizacao()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Música não encontrada com o ID: " + idMusica
                ));
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
