package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.exception.AlbumDuplicadoException;
import gerenciador_musica_backend.exception.DadosAlbumInvalidosException;
import gerenciador_musica_backend.service.AlbumService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAlbumController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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

    @Test
    void deveCadastrarAlbumComStatus201ELocation() throws Exception {
        when(albumService.cadastrarAlbum(any(AlbumRequestDTO.class)))
                .thenReturn(montarResposta());

        mockMvc.perform(post("/api/admin/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "titulo": "A Night at the Opera",
                                    "idArtista": 1,
                                    "anoLancamento": 1975
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/albuns/10"
                ))
                .andExpect(jsonPath("$.idAlbum").value(10))
                .andExpect(jsonPath("$.artista.id").value(1));
    }

    @Test
    void deveRetornar400QuandoArtistaNaoForInformado() throws Exception {
        mockMvc.perform(post("/api/admin/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "titulo": "A Night at the Opera",
                                    "anoLancamento": 1975
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.idArtista").exists());
    }

    @Test
    void deveRetornar409QuandoAlbumJaExistir() throws Exception {
        when(albumService.cadastrarAlbum(any(AlbumRequestDTO.class)))
                .thenThrow(new AlbumDuplicadoException(
                        "O álbum já está cadastrado."
                ));

        mockMvc.perform(post("/api/admin/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "titulo": "A Night at the Opera",
                                    "idArtista": 1,
                                    "anoLancamento": 1975
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("O álbum já está cadastrado."));
    }

    @Test
    void deveRetornar400QuandoServiceRejeitarOsDadosDoAlbum()
            throws Exception {
        when(albumService.cadastrarAlbum(any(AlbumRequestDTO.class)))
                .thenThrow(new DadosAlbumInvalidosException(
                        "O ano do álbum deve ser válido."
                ));

        mockMvc.perform(post("/api/admin/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "titulo": "A Night at the Opera",
                                    "idArtista": 1,
                                    "anoLancamento": 1975
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("O ano do álbum deve ser válido."))
                .andExpect(jsonPath("$.path")
                        .value("/api/admin/albuns"));
    }
}
