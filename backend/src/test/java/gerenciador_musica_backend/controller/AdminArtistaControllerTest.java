package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.ArtistaRequestDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.exception.ArtistaDuplicadoException;
import gerenciador_musica_backend.exception.ArtistaEmUsoException;
import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosArtistaInvalidosException;
import gerenciador_musica_backend.service.ArtistaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminArtistaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminArtistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistaService artistaService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String REQUEST_ATUALIZACAO = """
            {
                "nome": "Queen + Adam Lambert",
                "nomeCompleto": "Queen e Adam Lambert",
                "descricao": "Projeto musical em atividade.",
                "fotoPerfilUrl": "https://exemplo.com/queen-atualizado.jpg"
            }
            """;

    @Test
    void deveAtualizarArtistaComStatus200() throws Exception {
        when(artistaService.atualizarArtista(
                eq(1L),
                any(ArtistaRequestDTO.class)
        )).thenReturn(new ArtistaResponseDTO(
                1L,
                "Queen + Adam Lambert",
                "Queen e Adam Lambert",
                "Projeto musical em atividade.",
                "https://exemplo.com/queen-atualizado.jpg"
        ));

        mockMvc.perform(put("/api/admin/artistas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idArtista").value(1))
                .andExpect(jsonPath("$.nome")
                        .value("Queen + Adam Lambert"))
                .andExpect(jsonPath("$.nomeCompleto")
                        .value("Queen e Adam Lambert"))
                .andExpect(jsonPath("$.descricao")
                        .value("Projeto musical em atividade."))
                .andExpect(jsonPath("$.fotoPerfilUrl")
                        .value("https://exemplo.com/queen-atualizado.jpg"));

        verify(artistaService).atualizarArtista(
                eq(1L),
                any(ArtistaRequestDTO.class)
        );
    }

    @Test
    void deveRetornar400QuandoAtualizacaoForInvalida() throws Exception {
        mockMvc.perform(put("/api/admin/artistas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": " ",
                                    "nomeCompleto": "Queen",
                                    "descricao": "Descrição válida."
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists());
    }

    @Test
    void deveRetornar404AoAtualizarArtistaInexistente()
            throws Exception {
        when(artistaService.atualizarArtista(
                eq(99L),
                any(ArtistaRequestDTO.class)
        )).thenThrow(new ArtistaNaoEncontradoException(99L));

        mockMvc.perform(put("/api/admin/artistas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Artista não encontrado com o ID: 99"));
    }

    @Test
    void deveRetornar409AoAtualizarComNomeDuplicado()
            throws Exception {
        when(artistaService.atualizarArtista(
                eq(1L),
                any(ArtistaRequestDTO.class)
        )).thenThrow(new ArtistaDuplicadoException(
                "Esse artista já foi cadastrado: Queen + Adam Lambert"
        ));

        mockMvc.perform(put("/api/admin/artistas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_ATUALIZACAO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Esse artista já foi cadastrado: "
                                + "Queen + Adam Lambert"
                ));
    }

    @Test
    void deveExcluirArtistaComStatus204() throws Exception {
        mockMvc.perform(delete("/api/admin/artistas/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(artistaService).excluirArtista(1L);
    }

    @Test
    void deveRetornar400QuandoIdForInvalido() throws Exception {
        doThrow(new DadosArtistaInvalidosException(
                "O ID do artista deve ser positivo."
        )).when(artistaService).excluirArtista(0L);

        mockMvc.perform(delete("/api/admin/artistas/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("O ID do artista deve ser positivo."));
    }

    @Test
    void deveRetornar404QuandoArtistaNaoExistir() throws Exception {
        doThrow(new ArtistaNaoEncontradoException(99L))
                .when(artistaService)
                .excluirArtista(99L);

        mockMvc.perform(delete("/api/admin/artistas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Artista não encontrado com o ID: 99"));
    }

    @Test
    void deveRetornar409QuandoArtistaEstiverEmUso() throws Exception {
        String mensagem = "Não é possível excluir o artista porque "
                + "ele possui álbuns associados.";

        doThrow(new ArtistaEmUsoException(mensagem))
                .when(artistaService)
                .excluirArtista(1L);

        mockMvc.perform(delete("/api/admin/artistas/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(mensagem));
    }

    @Test
    void deveRetornar400QuandoIdNaoForNumerico() throws Exception {
        mockMvc.perform(delete("/api/admin/artistas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("O parâmetro 'idArtista' possui um valor inválido."));
    }
}
