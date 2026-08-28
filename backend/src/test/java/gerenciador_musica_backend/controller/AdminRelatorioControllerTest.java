package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.RelatorioAlbumDTO;
import gerenciador_musica_backend.dto.RelatorioArtistaDTO;
import gerenciador_musica_backend.dto.RelatorioCatalogoDTO;
import gerenciador_musica_backend.dto.ResumoCatalogoDTO;
import gerenciador_musica_backend.dto.TipoRelatorio;
import gerenciador_musica_backend.service.RelatorioCsvService;
import gerenciador_musica_backend.service.RelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRelatorioController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminRelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RelatorioService relatorioService;

    @MockitoBean
    private RelatorioCsvService relatorioCsvService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveRetornarRelatorioDoCatalogoSemCache() throws Exception {
        when(relatorioService.gerarRelatorioCatalogo())
                .thenReturn(montarRelatorio());

        mockMvc.perform(get("/api/admin/relatorios/catalogo"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CACHE_CONTROL,
                        containsString("no-store")
                ))
                .andExpect(jsonPath("$.resumo.totalArtistas").value(1))
                .andExpect(jsonPath("$.resumo.totalAlbuns").value(1))
                .andExpect(jsonPath("$.artistas[0].nome").value("Queen"))
                .andExpect(jsonPath("$.albuns[0].titulo")
                        .value("A Night at the Opera"));
    }

    @Test
    void deveExportarRelatorioDeArtistasComoCsv() throws Exception {
        byte[] conteudo = "relatório".getBytes(StandardCharsets.UTF_8);
        when(relatorioCsvService.exportar(TipoRelatorio.ARTISTAS))
                .thenReturn(conteudo);

        mockMvc.perform(get("/api/admin/relatorios/catalogo.csv")
                        .param("tipo", "ARTISTAS"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString("relatorio-artistas.csv")
                ))
                .andExpect(content().bytes(conteudo));

        verify(relatorioCsvService).exportar(TipoRelatorio.ARTISTAS);
    }

    @Test
    void deveRetornar400ParaTipoDeRelatorioInvalido() throws Exception {
        mockMvc.perform(get("/api/admin/relatorios/catalogo.csv")
                        .param("tipo", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    private RelatorioCatalogoDTO montarRelatorio() {
        return new RelatorioCatalogoDTO(
                OffsetDateTime.parse("2026-08-28T12:00:00Z"),
                new ResumoCatalogoDTO(1, 1, 2, 1, 420),
                List.of(new RelatorioArtistaDTO(
                        1L,
                        "Queen",
                        1,
                        2,
                        1,
                        420
                )),
                List.of(new RelatorioAlbumDTO(
                        1L,
                        "A Night at the Opera",
                        "Queen",
                        (short) 1975,
                        2,
                        420
                ))
        );
    }
}
