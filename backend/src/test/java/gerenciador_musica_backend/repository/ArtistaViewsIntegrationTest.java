package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Genero;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.service.ArtistaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
