package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.PaginaResponseDTO;
import gerenciador_musica_backend.dto.ReviewAlvoDTO;
import gerenciador_musica_backend.dto.ReviewAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.ReviewAutorDTO;
import gerenciador_musica_backend.dto.ReviewRequestDTO;
import gerenciador_musica_backend.dto.ReviewResponseDTO;
import gerenciador_musica_backend.exception.ReviewAcessoNegadoException;
import gerenciador_musica_backend.exception.ReviewNaoEncontradaException;
import gerenciador_musica_backend.model.TipoAlvoReview;
import gerenciador_musica_backend.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ReviewResponseDTO montarResposta() {
        return new ReviewResponseDTO(
                1L,
                new ReviewAutorDTO(1L, "Maria"),
                new ReviewAlvoDTO(
                        TipoAlvoReview.MUSICA,
                        20L,
                        "Bohemian Rhapsody",
                        "Queen",
                        "capa.jpg"
                ),
                (short) 5,
                "Obra-prima",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                true
        );
    }

    @Test
    void deveCriarReview() throws Exception {
        when(reviewService.criarReview(any())).thenReturn(montarResposta());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idMusica":20,"nota":5,"texto":"Obra-prima"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idReview").value(1))
                .andExpect(jsonPath("$.alvo.tipo").value("MUSICA"))
                .andExpect(jsonPath("$.autor.nome").value("Maria"));
    }

    @Test
    void deveAtualizarReview() throws Exception {
        when(reviewService.atualizarReview(eq(1L), any()))
                .thenReturn(montarResposta());

        mockMvc.perform(put("/api/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nota":5,"texto":"Obra-prima"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(5));

        verify(reviewService).atualizarReview(
                eq(1L),
                eq(new ReviewAtualizacaoRequestDTO((short) 5, "Obra-prima"))
        );
    }

    @Test
    void deveRetornar403AoAtualizarReviewDeOutroUsuario() throws Exception {
        when(reviewService.atualizarReview(eq(1L), any()))
                .thenThrow(new ReviewAcessoNegadoException(
                        "Você não possui permissão para alterar esta review."
                ));

        mockMvc.perform(put("/api/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nota":5,"texto":null}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveExcluirReview() throws Exception {
        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isNoContent());

        verify(reviewService).excluirReview(1L);
    }

    @Test
    void deveRetornar404AoExcluirReviewInexistente() throws Exception {
        org.mockito.Mockito.doThrow(new ReviewNaoEncontradaException(99L))
                .when(reviewService).excluirReview(99L);

        mockMvc.perform(delete("/api/reviews/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarFeed() throws Exception {
        when(reviewService.listarFeed(isNull(), isNull())).thenReturn(
                new PaginaResponseDTO<>(List.of(montarResposta()), 0, 20, 1, 1)
        );

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].idReview").value(1))
                .andExpect(jsonPath("$.totalItens").value(1));
    }

    @Test
    void deveListarMinhasReviews() throws Exception {
        when(reviewService.listarMinhas(isNull(), isNull())).thenReturn(
                new PaginaResponseDTO<>(List.of(montarResposta()), 0, 20, 1, 1)
        );

        mockMvc.perform(get("/api/reviews/minhas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].minhaReview").value(true));
    }

    @Test
    void deveListarReviewsPorMusica() throws Exception {
        when(reviewService.listarPorMusica(eq(20L), isNull(), isNull()))
                .thenReturn(new PaginaResponseDTO<>(List.of(montarResposta()), 0, 20, 1, 1));

        mockMvc.perform(get("/api/reviews/musicas/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].alvo.id").value(20));
    }

    @Test
    void deveListarReviewsPorAlbum() throws Exception {
        when(reviewService.listarPorAlbum(eq(10L), isNull(), isNull()))
                .thenReturn(new PaginaResponseDTO<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/reviews/albuns/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isEmpty());
    }
}
