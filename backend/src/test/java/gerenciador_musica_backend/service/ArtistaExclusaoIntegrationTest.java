package gerenciador_musica_backend.service;

import gerenciador_musica_backend.exception.ArtistaEmUsoException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Perfil;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.PerfilRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ArtistaExclusaoIntegrationTest {

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private MusicaRepository musicaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveExcluirArtistaLivreELimparDestaqueDoPerfil() {
        Artista artista = salvarArtista("Artista livre");
        Perfil perfil = salvarPerfilComDestaque(artista);

        artistaService.excluirArtista(artista.getIdArtista());
        artistaRepository.flush();

        assertThat(artistaRepository.existsById(
                artista.getIdArtista()
        )).isFalse();
        assertThat(perfilRepository.existsById(perfil.getId())).isTrue();
        assertThat(buscarArtistaDestaque(perfil.getId())).isNull();
    }

    @Test
    void naoDeveExcluirArtistaNemAlbumAssociado() {
        Artista artista = salvarArtista("Artista com álbum");
        Album album = albumRepository.saveAndFlush(
                new Album(
                        artista,
                        "Álbum de teste",
                        (short) 2026,
                        null
                )
        );

        assertThatThrownBy(() -> artistaService.excluirArtista(
                artista.getIdArtista()
        )).isInstanceOf(ArtistaEmUsoException.class);

        assertThat(artistaRepository.existsById(
                artista.getIdArtista()
        )).isTrue();
        assertThat(albumRepository.existsById(album.getIdAlbum())).isTrue();
    }

    @Test
    void naoDeveExcluirArtistaPrincipalNemMusicaAssociada() {
        Artista artista = salvarArtista("Artista principal");
        Musica musica = musicaRepository.saveAndFlush(
                novaMusica("Música principal", artista)
        );

        assertThatThrownBy(() -> artistaService.excluirArtista(
                artista.getIdArtista()
        )).isInstanceOf(ArtistaEmUsoException.class);

        assertThat(artistaRepository.existsById(
                artista.getIdArtista()
        )).isTrue();
        assertThat(musicaRepository.existsById(
                musica.getIdMusica()
        )).isTrue();
    }

    @Test
    void naoDeveExcluirArtistaParticipanteNemRemoverVinculo() {
        Artista participante = salvarArtista("Artista participante");
        Artista principal = salvarArtista("Outro artista principal");
        Musica musica = novaMusica("Música com participação", principal);
        musica.setArtistasParticipantes(Set.of(participante));
        musica = musicaRepository.saveAndFlush(musica);

        assertThatThrownBy(() -> artistaService.excluirArtista(
                participante.getIdArtista()
        )).isInstanceOf(ArtistaEmUsoException.class);

        assertThat(artistaRepository.existsById(
                participante.getIdArtista()
        )).isTrue();
        assertThat(musicaRepository.existsById(
                musica.getIdMusica()
        )).isTrue();
        assertThat(contarVinculoParticipante(
                musica.getIdMusica(),
                participante.getIdArtista()
        )).isEqualTo(1L);
    }

    private Artista salvarArtista(String nomeBase) {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);

        return artistaRepository.saveAndFlush(
                new Artista(
                        nomeBase + " " + sufixo,
                        nomeBase + " completo",
                        "Descrição de teste.",
                        null
                )
        );
    }

    private Perfil salvarPerfilComDestaque(Artista artista) {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Usuario usuario = usuarioRepository.saveAndFlush(
                new Usuario(
                        "Usuário de teste",
                        "perfil-" + sufixo + "@teste.com",
                        "senha-de-teste",
                        Role.USER
                )
        );
        Perfil perfil = perfilRepository.saveAndFlush(
                new Perfil(usuario)
        );

        jdbcTemplate.update(
                "UPDATE perfil SET id_artista_destaque = ? "
                        + "WHERE id_perfil = ?",
                artista.getIdArtista(),
                perfil.getId()
        );

        return perfil;
    }

    private Musica novaMusica(
            String titulo,
            Artista artistaPrincipal
    ) {
        return new Musica(
                titulo + " "
                        + UUID.randomUUID().toString().substring(0, 8),
                null,
                180,
                (short) 2026,
                artistaPrincipal,
                null
        );
    }

    private Long buscarArtistaDestaque(Long idPerfil) {
        return jdbcTemplate.queryForObject(
                "SELECT id_artista_destaque FROM perfil "
                        + "WHERE id_perfil = ?",
                Long.class,
                idPerfil
        );
    }

    private Long contarVinculoParticipante(
            Long idMusica,
            Long idArtista
    ) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM musica_artista "
                        + "WHERE id_musica = ? AND id_artista = ?",
                Long.class,
                idMusica,
                idArtista
        );
    }
}
