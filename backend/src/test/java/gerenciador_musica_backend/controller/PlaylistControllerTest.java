package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.config.JwtAuthenticationFilter;
import gerenciador_musica_backend.dto.PlaylistResponseDTO;
import gerenciador_musica_backend.exception.MusicaDuplicadaException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.exception.PlaylistAcessoNegadoException;
import gerenciador_musica_backend.exception.PlaylistNaoEncontradaException;
import gerenciador_musica_backend.service.PlaylistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Teste de INTEGRAÇÃO da camada web de playlists. O PlaylistService é
 * mockado; aqui validamos os status HTTP e o JSON de cada endpoint,
 * incluindo os erros (404, 403, 409) mapeados pelo PlaylistExceptionHandler.
 */
@WebMvcTest(PlaylistController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deveCriarPlaylistComSucesso() throws Exception {
        PlaylistResponseDTO resposta =
                new PlaylistResponseDTO(1L, "Favoritas", "Minhas músicas", List.of());

        when(playlistService.criarPlaylist(any())).thenReturn(resposta);

        mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Favoritas",
                                    "descricao": "Minhas músicas"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Favoritas"));
    }

    @Test
    void deveRetornar400QuandoNomeDaPlaylistEstaAusente() throws Exception {
        mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "descricao": "Sem nome"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarPlaylistsDoUsuario() throws Exception {
        PlaylistResponseDTO resposta =
                new PlaylistResponseDTO(1L, "Favoritas", null, List.of());

        when(playlistService.listarMinhasPlaylists()).thenReturn(List.of(resposta));

        mockMvc.perform(get("/api/playlists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Favoritas"));
    }

    @Test
    void deveBuscarPlaylistPorId() throws Exception {
        PlaylistResponseDTO resposta =
                new PlaylistResponseDTO(1L, "Favoritas", null, List.of());

        when(playlistService.buscarPlaylist(1L)).thenReturn(resposta);

        mockMvc.perform(get("/api/playlists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Favoritas"));
    }

    @Test
    void deveRetornar404QuandoPlaylistNaoExiste() throws Exception {
        when(playlistService.buscarPlaylist(999L))
                .thenThrow(new PlaylistNaoEncontradaException("Playlist não encontrada."));

        mockMvc.perform(get("/api/playlists/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar403QuandoPlaylistEhDeOutroUsuario() throws Exception {
        when(playlistService.buscarPlaylist(1L))
                .thenThrow(new PlaylistAcessoNegadoException(
                        "Você não possui permissão para acessar esta playlist."
                ));

        mockMvc.perform(get("/api/playlists/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveAdicionarMusicaNaPlaylist() throws Exception {
        doNothing().when(playlistService).adicionarMusica(1L, 5L);

        mockMvc.perform(post("/api/playlists/1/musicas/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar409QuandoMusicaJaEstaNaPlaylist() throws Exception {
        doThrow(new MusicaDuplicadaException("Esta música já está na playlist."))
                .when(playlistService).adicionarMusica(anyLong(), anyLong());

        mockMvc.perform(post("/api/playlists/1/musicas/5"))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRemoverMusicaDaPlaylist() throws Exception {
        doNothing().when(playlistService).removerMusica(1L, 5L);

        mockMvc.perform(delete("/api/playlists/1/musicas/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar404AoRemoverMusicaQueNaoEstaNaPlaylist() throws Exception {
        doThrow(new MusicaNaoEncontradaException(5L))
                .when(playlistService).removerMusica(anyLong(), anyLong());

        mockMvc.perform(delete("/api/playlists/1/musicas/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarPlaylistComSucesso() throws Exception {
        PlaylistResponseDTO resposta = new PlaylistResponseDTO(
                1L, "Favoritas atualizadas", "Nova descrição",
                "https://exemplo.com/capa.jpg", List.of()
        );

        when(playlistService.atualizarPlaylist(eq(1L), any())).thenReturn(resposta);

        mockMvc.perform(put("/api/playlists/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Favoritas atualizadas",
                                    "descricao": "Nova descrição",
                                    "capaUrl": "https://exemplo.com/capa.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Favoritas atualizadas"))
                .andExpect(jsonPath("$.capaUrl").value("https://exemplo.com/capa.jpg"));
    }

    @Test
    void deveRetornar403AoAtualizarPlaylistDeOutroUsuario() throws Exception {
        when(playlistService.atualizarPlaylist(eq(1L), any()))
                .thenThrow(new PlaylistAcessoNegadoException(
                        "Você não possui permissão para acessar esta playlist."
                ));

        mockMvc.perform(put("/api/playlists/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Novo nome"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveExcluirPlaylistComSucesso() throws Exception {
        doNothing().when(playlistService).excluirPlaylist(1L);

        mockMvc.perform(delete("/api/playlists/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar404AoExcluirPlaylistInexistente() throws Exception {
        doThrow(new PlaylistNaoEncontradaException("Playlist não encontrada."))
                .when(playlistService).excluirPlaylist(999L);

        mockMvc.perform(delete("/api/playlists/999"))
                .andExpect(status().isNotFound());
    }
}
