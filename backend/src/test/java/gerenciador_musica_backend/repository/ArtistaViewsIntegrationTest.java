package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Genero;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.service.AlbumService;
import gerenciador_musica_backend.service.ArtistaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * ArtistaService.buscarDetalhesCatalogo descobre o usuário logado através
 * do SecurityContextHolder (para enriquecer os álbuns com "curtida"), então
 * simulamos a autenticação antes de cada teste, igual ao PlaylistServiceTest.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ArtistaViewsIntegrationTest {

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private MusicaRepository musicaRepository;

    @Autowired
    private GeneroRepository generoRepository;

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private AlbumService albumService;

    @BeforeEach
    void autenticar() {
        Usuario usuarioLogado =
                new Usuario("Maria", "maria@email.com", "hash", Role.USER);
        ReflectionTestUtils.setField(usuarioLogado, "id", 1L);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuarioLogado, null, List.of()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveReunirDadosDasTresViewsNaPaginaDoArtista() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);

        Artista principal = artistaRepository.saveAndFlush(new Artista(
                "Artista principal " + sufixo,
                "Nome principal " + sufixo,
                "Descrição do artista principal.",
                null
        ));
        Artista participante = artistaRepository.saveAndFlush(new Artista(
                "Artista participante " + sufixo,
                "Nome participante " + sufixo,
                "Descrição do artista participante.",
                null
        ));
        Album album = albumRepository.saveAndFlush(new Album(
                principal,
                "Álbum das views " + sufixo,
                (short) 2026,
                "https://exemplo.com/capa.jpg"
        ));
        Genero genero = generoRepository.saveAndFlush(
                new Genero("Gênero views " + sufixo)
        );

        Musica musica = new Musica(
                "Música das views " + sufixo,
                null,
                210,
                (short) 2026,
                principal,
                album
        );
        musica.setArtistasParticipantes(Set.of(participante));
        musica.setGeneros(Set.of(genero));
        musicaRepository.saveAndFlush(musica);

        var detalhesPrincipal = artistaService.buscarDetalhesCatalogo(
                principal.getIdArtista()
        );

        assertThat(detalhesPrincipal.artista().totalAlbuns()).isEqualTo(1L);
        assertThat(detalhesPrincipal.artista().totalMusicasPrincipais())
                .isEqualTo(1L);
        assertThat(detalhesPrincipal.artista().totalParticipacoes()).isZero();
        assertThat(detalhesPrincipal.artista().duracaoTotalSegundos())
                .isEqualTo(210L);
        assertThat(detalhesPrincipal.albuns())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.titulo()).isEqualTo(album.getTitulo());
                    assertThat(item.totalMusicas()).isEqualTo(1L);
                });
        assertThat(detalhesPrincipal.musicas())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.titulo()).isEqualTo(musica.getTitulo());
                    assertThat(item.papelArtista()).isEqualTo("PRINCIPAL");
                    assertThat(item.generos()).containsExactly(genero.getNome());
                });

        var detalhesAlbum = albumService.buscarDetalhesCatalogo(
                album.getIdAlbum()
        );

        assertThat(detalhesAlbum.album().titulo())
                .isEqualTo(album.getTitulo());
        assertThat(detalhesAlbum.album().totalMusicas()).isEqualTo(1L);
        assertThat(detalhesAlbum.album().duracaoTotalSegundos())
                .isEqualTo(210L);
        assertThat(detalhesAlbum.generos())
                .containsExactly(genero.getNome());
        assertThat(detalhesAlbum.musicas())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.titulo()).isEqualTo(musica.getTitulo());
                    assertThat(item.duracaoSegundos()).isEqualTo(210);
                });

        var detalhesParticipante = artistaService.buscarDetalhesCatalogo(
                participante.getIdArtista()
        );

        assertThat(detalhesParticipante.artista().totalParticipacoes())
                .isEqualTo(1L);
        assertThat(detalhesParticipante.albuns()).isEmpty();
        assertThat(detalhesParticipante.musicas())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.papelArtista())
                            .isEqualTo("PARTICIPANTE");
                    assertThat(item.nomeArtistaPrincipal())
                            .isEqualTo(principal.getNome());
                });
    }
}
