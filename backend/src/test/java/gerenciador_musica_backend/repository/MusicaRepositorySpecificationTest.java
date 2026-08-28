package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.dto.MusicaFiltroDTO;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Genero;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.repository.specification.MusicaSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Teste de INTEGRAÇÃO: usa o Postgres de testes do docker-compose, não um banco
 * em memória nem o banco principal de desenvolvimento.
 * Por isso cada consulta é restrita aos IDs criados neste teste (ver porIdsCriados),
 * senão os asserts de contagem quebrariam com os dados que já existem no banco.
 * Cada teste roda dentro de uma transação revertida ao final (padrão do @DataJpaTest),
 * então nada fica gravado no banco depois da execução.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MusicaRepositorySpecificationTest {

    @Autowired
    private MusicaRepository musicaRepository;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private GeneroRepository generoRepository;

    private Artista artistaA;
    private Artista artistaB;
    private Genero rock;
    private Genero pop;
    private Set<Long> idsCriados;

    @BeforeEach
    void montarCenario() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);

        artistaA = artistaRepository.save(
                new Artista("Artista Teste A " + sufixo, "Artista Teste A Completo", "desc", null)
        );
        artistaB = artistaRepository.save(
                new Artista("Artista Teste B " + sufixo, "Artista Teste B Completo", "desc", null)
        );

        Album album = albumRepository.save(
                new Album(artistaA, "Album Teste " + sufixo, (short) 2020, "http://capa.png")
        );

        // nomes de gênero têm restrição UNIQUE no banco, por isso levam sufixo aleatório
        rock = generoRepository.save(new Genero("Rock Teste " + sufixo));
        pop = generoRepository.save(new Genero("Pop Teste " + sufixo));

        Musica amorEterno = new Musica(
                "Amor Eterno " + sufixo, null, 200, (short) 2020,
                artistaA, album
        );
        amorEterno.setGeneros(Set.of(rock));
        amorEterno = musicaRepository.save(amorEterno);

        Musica outraCancao = new Musica(
                "Outra Cancao " + sufixo, null, 180, (short) 2021,
                artistaB, null
        );
        outraCancao.setArtistasParticipantes(Set.of(artistaA));
        outraCancao.setGeneros(Set.of(pop));
        outraCancao = musicaRepository.save(outraCancao);

        Musica amorDeVerao = new Musica(
                "Amor de Verao " + sufixo, null, 190, (short) 2020,
                artistaB, null
        );
        amorDeVerao.setGeneros(new LinkedHashSet<>(Set.of(rock, pop)));
        amorDeVerao = musicaRepository.save(amorDeVerao);

        idsCriados = Set.of(
                amorEterno.getIdMusica(),
                outraCancao.getIdMusica(),
                amorDeVerao.getIdMusica()
        );
    }

    private Specification<Musica> comFiltroEscopadoAoTeste(MusicaFiltroDTO filtro) {
        return MusicaSpecification.comFiltros(filtro).and(porIdsCriados());
    }

    private Specification<Musica> porIdsCriados() {
        return (root, query, cb) -> root.get("idMusica").in(idsCriados);
    }

    @Test
    void filtraPorTituloParcialCaseInsensitive() {
        var filtro = new MusicaFiltroDTO("amor", null, null, null, null);

        List<Musica> resultado = musicaRepository.findAll(comFiltroEscopadoAoTeste(filtro));

        assertThat(resultado)
                .extracting(Musica::getTitulo)
                .allMatch(titulo -> titulo.toLowerCase().contains("amor"))
                .hasSize(2);
    }

    @Test
    void filtraPorArtistaConsiderandoTambemParticipantes() {
        var filtro = new MusicaFiltroDTO(null, artistaA.getIdArtista(), null, null, null);

        List<Musica> resultado = musicaRepository.findAll(comFiltroEscopadoAoTeste(filtro));

        assertThat(resultado)
                .extracting(Musica::getTitulo)
                .hasSize(2)
                .anyMatch(titulo -> titulo.startsWith("Amor Eterno"))
                .anyMatch(titulo -> titulo.startsWith("Outra Cancao"));
    }

    @Test
    void filtraPorGeneroSemDuplicarMusicaComDoisGeneros() {
        var filtro = new MusicaFiltroDTO(null, null, null, rock.getIdGenero(), null);

        List<Musica> resultado = musicaRepository.findAll(comFiltroEscopadoAoTeste(filtro));

        assertThat(resultado)
                .extracting(Musica::getTitulo)
                .hasSize(2)
                .anyMatch(titulo -> titulo.startsWith("Amor Eterno"))
                .anyMatch(titulo -> titulo.startsWith("Amor de Verao"));
    }

    @Test
    void combinaTituloEAnoComAND() {
        var filtro = new MusicaFiltroDTO("amor", null, null, null, (short) 2020);

        List<Musica> resultado = musicaRepository.findAll(comFiltroEscopadoAoTeste(filtro));

        assertThat(resultado).hasSize(2);
    }

    @Test
    void combinaTituloEGeneroRestringindoParaUmaUnicaMusica() {
        var filtro = new MusicaFiltroDTO("amor", null, null, pop.getIdGenero(), null);

        List<Musica> resultado = musicaRepository.findAll(comFiltroEscopadoAoTeste(filtro));

        assertThat(resultado)
                .extracting(Musica::getTitulo)
                .hasSize(1)
                .allMatch(titulo -> titulo.startsWith("Amor de Verao"));
    }

    @Test
    void ignoraFiltroDeTituloComEspacosEmBranco() {
        var filtro = new MusicaFiltroDTO("   ", null, null, null, null);

        List<Musica> resultado = musicaRepository.findAll(comFiltroEscopadoAoTeste(filtro));

        assertThat(resultado).hasSize(3);
    }

    @Test
    void paginaComDistinctContaTotalCorretoMesmoComJoins() {
        var filtro = new MusicaFiltroDTO(null, null, null, null, (short) 2020);

        Page<Musica> pagina = musicaRepository.findAll(
                comFiltroEscopadoAoTeste(filtro),
                PageRequest.of(0, 1)
        );

        assertThat(pagina.getTotalElements()).isEqualTo(2);
        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
    }

    @Test
    void semFiltrosRetornaTodasAsMusicasCriadasSemDuplicar() {
        var filtro = new MusicaFiltroDTO(null, null, null, null, null);

        List<Musica> resultado = musicaRepository.findAll(comFiltroEscopadoAoTeste(filtro));

        assertThat(resultado).hasSize(3);
    }
}
