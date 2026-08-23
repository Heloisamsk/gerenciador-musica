package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.dto.MusicaRequestDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.exception.DadosMusicaInvalidosException;
import gerenciador_musica_backend.exception.MusicaDuplicadaException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.service.MusicaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Teste de INTEGRAÇÃO da camada web do CRUD administrativo de
 * músicas. O MusicaService é mockado.
 */
@WebMvcTest(AdminMusicaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMusicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MusicaService musicaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CORPO_VALIDO = """
            {
                "titulo": "Bohemian Rhapsody",
                "duracaoSegundos": 354,
                "anoLancamento": 1975,
                "artistaPrincipalId": 1,
                "artistasParticipantesIds": [],
                "albumId": 1,
                "generos": ["Rock"]
            }
            """;

    private MusicaResponseDTO montarResposta() {
        return new MusicaResponseDTO(
                1L,
                "Bohemian Rhapsody",
                null,
                354,
                (short) 1975,
                new ArtistaResumoDTO(
                        1L,
                        "Queen",
                        "Queen",
                        "Banda britânica de rock.",
                        null
                ),
                null,
                Set.of(),
                Set.of()
        );
    }

    @Test
    void deveCadastrarMusicaComSucesso() throws Exception {
        when(musicaService.cadastrarMusica(any(MusicaRequestDTO.class)))
                .thenReturn(montarResposta());

        mockMvc.perform(post("/api/admin/musicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.titulo")
                        .value("Bohemian Rhapsody"))
                .andExpect(jsonPath("$.artistaPrincipal.nomeCompleto")
                        .value("Queen"))
                .andExpect(jsonPath("$.artistaPrincipal.descricao")
                        .value("Banda britânica de rock."));
    }

    @Test
    void deveRetornar400QuandoTituloEstaAusente() throws Exception {
        mockMvc.perform(post("/api/admin/musicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "duracaoSegundos": 354,
                                    "anoLancamento": 1975,
                                    "artistaPrincipalId": 1,
                                    "artistasParticipantesIds": [],
                                    "generos": ["Rock"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.titulo").exists());
    }

    @Test
    void deveRetornar409QuandoMusicaJaCadastrada() throws Exception {
        when(musicaService.cadastrarMusica(any(MusicaRequestDTO.class)))
                .thenThrow(new MusicaDuplicadaException(
                        "A música já está cadastrada."
                ));

        mockMvc.perform(post("/api/admin/musicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A música já está cadastrada."));
    }

    @Test
    void deveAtualizarMusicaComStatus200() throws Exception {
        when(musicaService.atualizarMusica(
                eq(1L),
                any(MusicaRequestDTO.class)
        )).thenReturn(montarResposta());

        mockMvc.perform(put("/api/admin/musicas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo")
                        .value("Bohemian Rhapsody"));

        verify(musicaService).atualizarMusica(
                eq(1L),
                any(MusicaRequestDTO.class)
        );
    }

    @Test
    void deveRetornar400AoAtualizarComDadosInvalidos()
            throws Exception {
        mockMvc.perform(put("/api/admin/musicas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "titulo": "   ",
                                    "duracaoSegundos": 0,
                                    "anoLancamento": 1700,
                                    "artistaPrincipalId": 1,
                                    "artistasParticipantesIds": [],
                                    "generos": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.titulo").exists())
                .andExpect(jsonPath("$.fieldErrors.duracaoSegundos")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.anoLancamento")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.generos").exists());
    }

    @Test
    void deveRetornar404AoAtualizarMusicaInexistente()
            throws Exception {
        when(musicaService.atualizarMusica(
                eq(99L),
                any(MusicaRequestDTO.class)
        )).thenThrow(new MusicaNaoEncontradaException(99L));

        mockMvc.perform(put("/api/admin/musicas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Música não encontrada com o ID: 99"
                ));
    }

    @Test
    void deveRetornar409AoAtualizarParaOutraMusicaDuplicada()
            throws Exception {
        when(musicaService.atualizarMusica(
                eq(1L),
                any(MusicaRequestDTO.class)
        )).thenThrow(new MusicaDuplicadaException(
                "A música já está cadastrada."
        ));

        mockMvc.perform(put("/api/admin/musicas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A música já está cadastrada."));
    }

    @Test
    void deveExcluirMusicaComStatus204() throws Exception {
        mockMvc.perform(delete("/api/admin/musicas/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(musicaService).excluirMusica(1L);
    }

    @Test
    void deveRetornar404AoExcluirMusicaInexistente()
            throws Exception {
        doThrow(new MusicaNaoEncontradaException(99L))
                .when(musicaService)
                .excluirMusica(99L);

        mockMvc.perform(delete("/api/admin/musicas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Música não encontrada com o ID: 99"
                ));
    }

    @Test
    void deveRetornar400AoExcluirComIdInvalido() throws Exception {
        doThrow(new DadosMusicaInvalidosException(
                "O ID da música deve ser positivo."
        ))
                .when(musicaService)
                .excluirMusica(0L);

        mockMvc.perform(delete("/api/admin/musicas/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "O ID da música deve ser positivo."
                ));
    }
}
