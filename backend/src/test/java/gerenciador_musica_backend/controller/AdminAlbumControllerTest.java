package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.AlbumAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.exception.AlbumDuplicadoException;
import gerenciador_musica_backend.exception.AlbumEmUsoException;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void deveAtualizarAlbumComStatus200() throws Exception {
        when(albumService.atualizarAlbum(
                eq(10L),
                any(AlbumAtualizacaoRequestDTO.class)
        )).thenReturn(montarResposta());

        mockMvc.perform(put("/api/admin/albuns/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "titulo": "A Night at the Opera",
                                    "anoLancamento": 1975,
                                    "capaUrl": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlbum").value(10))
                .andExpect(jsonPath("$.titulo")
                        .value("A Night at the Opera"))
                .andExpect(jsonPath("$.artista.id").value(1));

        verify(albumService).atualizarAlbum(
                eq(10L),
                any(AlbumAtualizacaoRequestDTO.class)
        );
    }

    @Test
    void deveRetornar400QuandoAtualizacaoForInvalida()
            throws Exception {
        mockMvc.perform(put("/api/admin/albuns/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "titulo": "   ",
                                    "anoLancamento": 1799,
                                    "capaUrl": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.titulo").exists())
                .andExpect(jsonPath("$.fieldErrors.anoLancamento")
                        .exists());
    }

    @Test
    void deveRetornar400QuandoIdDaAtualizacaoForInvalido()
            throws Exception {
        when(albumService.atualizarAlbum(
                eq(0L),
                any(AlbumAtualizacaoRequestDTO.class)
        )).thenThrow(new DadosAlbumInvalidosException(
                "O ID do álbum deve ser válido."
        ));

        mockMvc.perform(put("/api/admin/albuns/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "titulo": "A Night at the Opera",
                                    "anoLancamento": 1975,
                                    "capaUrl": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("O ID do álbum deve ser válido."));
    }

    @Test
    void deveRetornar404QuandoAtualizarAlbumInexistente()
            throws Exception {
        when(albumService.atualizarAlbum(
                eq(99L),
                any(AlbumAtualizacaoRequestDTO.class)
        )).thenThrow(new AlbumNaoEncontradoException(99L));

        mockMvc.perform(put("/api/admin/albuns/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "titulo": "A Night at the Opera",
                                    "anoLancamento": 1975,
                                    "capaUrl": null
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Álbum não encontrado com o ID: 99"));
    }

    @Test
    void deveRetornar409QuandoAtualizacaoGerarDuplicidade()
            throws Exception {
        when(albumService.atualizarAlbum(
                eq(10L),
                any(AlbumAtualizacaoRequestDTO.class)
        )).thenThrow(new AlbumDuplicadoException(
                "Já existe outro álbum com esses dados."
        ));

        mockMvc.perform(put("/api/admin/albuns/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "titulo": "A Night at the Opera",
                                    "anoLancamento": 1975,
                                    "capaUrl": null
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Já existe outro álbum com esses dados."));
    }

    @Test
    void deveExcluirAlbumComStatus204() throws Exception {
        mockMvc.perform(delete("/api/admin/albuns/10"))
                .andExpect(status().isNoContent());

        verify(albumService).excluirAlbum(10L);
    }

    @Test
    void deveRetornar404AoExcluirAlbumInexistente() throws Exception {
        doThrow(new AlbumNaoEncontradoException(99L))
                .when(albumService)
                .excluirAlbum(99L);

        mockMvc.perform(delete("/api/admin/albuns/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Álbum não encontrado com o ID: 99"));
    }

    @Test
    void deveRetornar409QuandoAlbumEstiverEmUso() throws Exception {
        doThrow(new AlbumEmUsoException(
                "Não é possível excluir o álbum porque "
                        + "ele possui músicas associadas."
        ))
                .when(albumService)
                .excluirAlbum(10L);

        mockMvc.perform(delete("/api/admin/albuns/10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Não é possível excluir o álbum porque "
                                + "ele possui músicas associadas."
                ));
    }
}
