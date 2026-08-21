package gerenciador_musica_backend.controller;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import gerenciador_musica_backend.dto.MusicaListagemDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.dto.PaginaResponseDTO;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Genero;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.GeneroRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import gerenciador_musica_backend.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Teste de INTEGRAÇÃO de ponta a ponta (US06 / #118): sobe o contexto Spring
 * inteiro e faz requisições HTTP simuladas (MockMvc) contra GET /api/musicas
 * e GET /api/musicas/{id} passando pela cadeia de segurança (JWT) de verdade
 * — diferente do MusicaControllerTest, que desliga os filtros de segurança
 * e mocka o service.
 *
 * Usa o Postgres real do docker-compose (mesmo banco de desenvolvimento),
 * então os dados de cada teste levam um sufixo aleatório e são apagados no
 * @AfterEach para não sujar o banco compartilhado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MusicaPesquisaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MusicaRepository musicaRepository;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private GeneroRepository generoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String sufixo;
    private Artista artistaA;
    private Artista artistaB;
    private Album album;
    private Genero rock;
    private Genero pop;
    private Musica amorEterno;
    private Musica outraCancao;
    private Musica amorDeVerao;
    private Usuario usuarioComum;
    private Usuario usuarioAdmin;
    private String tokenUsuarioComum;
    private String tokenAdmin;

    @BeforeEach
    void montarCenario() {
        sufixo = UUID.randomUUID().toString().substring(0, 8);

        artistaA = artistaRepository.save(
                new Artista("Artista Teste A " + sufixo, "Artista Teste A Completo", "desc", null)
        );
        artistaB = artistaRepository.save(
                new Artista("Artista Teste B " + sufixo, "Artista Teste B Completo", "desc", null)
        );

        album = albumRepository.save(
                new Album(artistaA, "Album Teste " + sufixo, (short) 2020, "http://capa.png")
        );

        rock = generoRepository.save(new Genero("Rock Teste " + sufixo));
        pop = generoRepository.save(new Genero("Pop Teste " + sufixo));

        amorEterno = new Musica(
                "Amor Eterno " + sufixo, "Letra de Amor Eterno", 200, (short) 2020,
                artistaA, album
        );
        amorEterno.setGeneros(Set.of(rock));
        amorEterno = musicaRepository.save(amorEterno);

        outraCancao = new Musica(
                "Outra Cancao " + sufixo, null, 180, (short) 2021,
                artistaB, null
        );
        outraCancao.setArtistasParticipantes(Set.of(artistaA));
        outraCancao.setGeneros(Set.of(pop));
        outraCancao = musicaRepository.save(outraCancao);

        amorDeVerao = new Musica(
                "Amor de Verao " + sufixo, null, 190, (short) 2020,
                artistaB, null
        );
        amorDeVerao.setGeneros(new LinkedHashSet<>(Set.of(rock, pop)));
        amorDeVerao = musicaRepository.save(amorDeVerao);

        usuarioComum = usuarioRepository.save(new Usuario(
                "Usuario Teste " + sufixo,
                "usuario" + sufixo + "@teste.com",
                passwordEncoder.encode("Senha@123"),
                Role.USER
        ));
        usuarioAdmin = usuarioRepository.save(new Usuario(
                "Admin Teste " + sufixo,
                "admin" + sufixo + "@teste.com",
                passwordEncoder.encode("Senha@123"),
                Role.ADMIN
        ));

        tokenUsuarioComum = jwtService.gerarToken(usuarioComum);
        tokenAdmin = jwtService.gerarToken(usuarioAdmin);
    }

    @AfterEach
    void limparCenario() {
        musicaRepository.deleteAll(List.of(amorEterno, outraCancao, amorDeVerao));
        usuarioRepository.deleteAll(List.of(usuarioComum, usuarioAdmin));
        albumRepository.delete(album);
        artistaRepository.deleteAll(List.of(artistaA, artistaB));
        generoRepository.deleteAll(List.of(rock, pop));
    }

    /*
     * Usa .param(...) em vez de montar a query string manualmente: os
     * valores são passados como texto puro (sem precisar de URL-encoding),
     * e o MockMvc os entrega ao controller já decodificados corretamente.
     */
    private MockHttpServletResponse get(String path, String token, Map<String, String> params) throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get(path);

        params.forEach(requestBuilder::param);

        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        return mockMvc.perform(requestBuilder).andReturn().getResponse();
    }

    private MockHttpServletResponse get(String path, String token) throws Exception {
        return get(path, token, Map.of());
    }

    private PaginaResponseDTO<MusicaListagemDTO> pesquisar(Map<String, String> params) throws Exception {
        MockHttpServletResponse resposta = get("/api/musicas", tokenUsuarioComum, params);

        assertThat(resposta.getStatus()).isEqualTo(200);

        return objectMapper.readValue(
                resposta.getContentAsString(),
                new TypeReference<PaginaResponseDTO<MusicaListagemDTO>>() {}
        );
    }

    private Map<String, String> params(String... nomesEValores) {
        Map<String, String> mapa = new LinkedHashMap<>();

        for (int i = 0; i < nomesEValores.length; i += 2) {
            mapa.put(nomesEValores[i], nomesEValores[i + 1]);
        }

        return mapa;
    }

    @Test
    void deveEncontrarMusicaPorTituloParcial() throws Exception {
        var resultado = pesquisar(params("titulo", "Eterno " + sufixo));

        assertThat(resultado.itens())
                .extracting(MusicaListagemDTO::titulo)
                .containsExactly(amorEterno.getTitulo());
    }

    @Test
    void deveEncontrarMusicaIgnorandoCaixaDoTitulo() throws Exception {
        var resultado = pesquisar(params("titulo", ("ETERNO " + sufixo).toUpperCase()));

        assertThat(resultado.itens())
                .extracting(MusicaListagemDTO::titulo)
                .containsExactly(amorEterno.getTitulo());
    }

    @Test
    void deveFiltrarPorArtistaPrincipal() throws Exception {
        var resultado = pesquisar(params("artistaId", artistaA.getIdArtista().toString()));

        assertThat(resultado.itens())
                .extracting(MusicaListagemDTO::titulo)
                .contains(amorEterno.getTitulo());
    }

    @Test
    void deveFiltrarPorArtistaParticipante() throws Exception {
        var resultado = pesquisar(params("artistaId", artistaA.getIdArtista().toString()));

        assertThat(resultado.itens())
                .extracting(MusicaListagemDTO::titulo)
                .contains(outraCancao.getTitulo());
    }

    @Test
    void deveFiltrarPorAlbum() throws Exception {
        var resultado = pesquisar(params("albumId", album.getIdAlbum().toString()));

        assertThat(resultado.itens())
                .extracting(MusicaListagemDTO::titulo)
                .containsExactly(amorEterno.getTitulo());
    }

    @Test
    void deveFiltrarPorGenero() throws Exception {
        var resultado = pesquisar(params("generoId", rock.getIdGenero().toString()));

        assertThat(resultado.itens())
                .extracting(MusicaListagemDTO::titulo)
                .containsExactlyInAnyOrder(amorEterno.getTitulo(), amorDeVerao.getTitulo());
    }

    @Test
    void deveFiltrarPorAno() throws Exception {
        var resultado = pesquisar(params("titulo", sufixo, "ano", "2020"));

        assertThat(resultado.itens())
                .extracting(MusicaListagemDTO::titulo)
                .containsExactlyInAnyOrder(amorEterno.getTitulo(), amorDeVerao.getTitulo());
    }

    @Test
    void deveCombinarMultiplosFiltrosComAND() throws Exception {
        var resultado = pesquisar(
                params("titulo", sufixo, "ano", "2020", "generoId", pop.getIdGenero().toString())
        );

        assertThat(resultado.itens())
                .extracting(MusicaListagemDTO::titulo)
                .containsExactly(amorDeVerao.getTitulo());
    }

    @Test
    void naoDeveRetornarMusicaDuplicadaQuandoTemMultiplosGeneros() throws Exception {
        // amorDeVerao tem 2 gêneros (rock e pop); sem o distinct no JOIN ela
        // apareceria duas vezes na listagem.
        var resultado = pesquisar(params("titulo", sufixo));

        assertThat(resultado.itens()).hasSize(3);
        assertThat(resultado.itens())
                .extracting(MusicaListagemDTO::titulo)
                .filteredOn(titulo -> titulo.equals(amorDeVerao.getTitulo()))
                .hasSize(1);
    }

    @Test
    void deveRetornarResultadoVazioQuandoNadaCorresponde() throws Exception {
        var resultado = pesquisar(params("titulo", sufixo, "ano", "1900"));

        assertThat(resultado.itens()).isEmpty();
        assertThat(resultado.totalItens()).isZero();
    }

    @Test
    void devePaginarEOrdenarCorretamente() throws Exception {
        // durações distintas (180/190/200s) evitam empate na ordenação.
        var pagina0 = pesquisar(params("titulo", sufixo, "size", "1", "sort", "duracaoSegundos,asc"));
        var pagina1 = pesquisar(params("titulo", sufixo, "size", "1", "page", "1", "sort", "duracaoSegundos,asc"));
        var pagina2 = pesquisar(params("titulo", sufixo, "size", "1", "page", "2", "sort", "duracaoSegundos,asc"));

        assertThat(pagina0.totalPaginas()).isEqualTo(3);
        assertThat(pagina0.itens()).extracting(MusicaListagemDTO::titulo)
                .containsExactly(outraCancao.getTitulo());
        assertThat(pagina1.itens()).extracting(MusicaListagemDTO::titulo)
                .containsExactly(amorDeVerao.getTitulo());
        assertThat(pagina2.itens()).extracting(MusicaListagemDTO::titulo)
                .containsExactly(amorEterno.getTitulo());
    }

    @Test
    void deveRetornar400QuandoAnoDoFiltroForInvalido() throws Exception {
        var resposta = get("/api/musicas", tokenUsuarioComum, params("ano", "3000"));

        assertThat(resposta.getStatus()).isEqualTo(400);
    }

    @Test
    void deveRetornar400QuandoPaginaForNegativa() throws Exception {
        var resposta = get("/api/musicas", tokenUsuarioComum, params("page", "-1"));

        assertThat(resposta.getStatus()).isEqualTo(400);
    }

    @Test
    void deveBuscarDetalheDeMusicaExistente() throws Exception {
        var resposta = get("/api/musicas/" + amorEterno.getIdMusica(), tokenUsuarioComum);

        assertThat(resposta.getStatus()).isEqualTo(200);

        MusicaResponseDTO musica = objectMapper.readValue(
                resposta.getContentAsString(), MusicaResponseDTO.class
        );

        assertThat(musica.titulo()).isEqualTo(amorEterno.getTitulo());
        assertThat(musica.letra()).isEqualTo("Letra de Amor Eterno");
        assertThat(musica.artistaPrincipal().nome()).isEqualTo(artistaA.getNome());
    }

    @Test
    void deveRetornar404QuandoDetalheDeMusicaNaoExiste() throws Exception {
        var resposta = get("/api/musicas/999999999", tokenUsuarioComum);

        assertThat(resposta.getStatus()).isEqualTo(404);
    }

    @Test
    void deveRetornar401QuandoNaoEnviaToken() throws Exception {
        var resposta = get("/api/musicas", null);

        assertThat(resposta.getStatus()).isEqualTo(401);
    }

    @Test
    void devePesquisarComTokenDeUsuarioComum() throws Exception {
        var resposta = get("/api/musicas", tokenUsuarioComum, params("titulo", sufixo));

        assertThat(resposta.getStatus()).isEqualTo(200);
    }

    @Test
    void devePesquisarComTokenDeAdmin() throws Exception {
        var resposta = get("/api/musicas", tokenAdmin, params("titulo", sufixo));

        assertThat(resposta.getStatus()).isEqualTo(200);
    }

    @Test
    void naoDeveSerializarEntidadesJpaDiretamente() throws Exception {
        var resposta = get("/api/musicas", tokenUsuarioComum, params("titulo", sufixo));
        String corpo = resposta.getContentAsString();

        assertThat(corpo)
                .doesNotContain("idMusica", "idArtista", "idAlbum", "idGenero")
                .contains("\"id\":", "\"artistaPrincipal\":", "\"generos\":");
    }
}
