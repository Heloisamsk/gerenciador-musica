package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.MusicaRequestDTO;
import gerenciador_musica_backend.dto.MusicaResponseDTO;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Genero;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Perfil;
import gerenciador_musica_backend.model.Playlist;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.GeneroRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.PerfilRepository;
import gerenciador_musica_backend.repository.PlaylistRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MusicaAtualizacaoExclusaoIntegrationTest {

    @Autowired
    private MusicaService musicaService;

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
    private PlaylistRepository playlistRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void devePersistirAtualizacaoCompletaSemCriarOutraMusica() {
        String sufixo = sufixoUnico();
        Artista artistaAnterior = salvarArtista(
                "Artista anterior " + sufixo
        );
        Artista artistaNovo = salvarArtista("Artista novo " + sufixo);
        Artista participanteAnterior = salvarArtista(
                "Participante anterior " + sufixo
        );
        Artista participanteNovo = salvarArtista(
                "Participante novo " + sufixo
        );
        Album albumAnterior = salvarAlbum(
                artistaAnterior,
                "Álbum anterior " + sufixo
        );
        Album albumNovo = salvarAlbum(
                artistaNovo,
                "Álbum novo " + sufixo
        );
        Genero generoAnterior = generoRepository.saveAndFlush(
                new Genero("Gênero anterior " + sufixo)
        );
        Genero generoNovo = generoRepository.saveAndFlush(
                new Genero("Gênero novo " + sufixo)
        );
        Musica musica = new Musica(
                "Música anterior " + sufixo,
                null,
                180,
                (short) 2020,
                artistaAnterior,
                albumAnterior
        );
        musica.setArtistasParticipantes(Set.of(participanteAnterior));
        musica.setGeneros(Set.of(generoAnterior));
        musica = musicaRepository.saveAndFlush(musica);
        Long idMusica = musica.getIdMusica();
        long quantidadeAntes = musicaRepository.count();

        Usuario usuario = salvarUsuario("edicao-musica-" + sufixo);
        Playlist playlist = salvarPlaylistComMusica(usuario, musica);

        MusicaResponseDTO response = musicaService.atualizarMusica(
                idMusica,
                new MusicaRequestDTO(
                        "  Música   atualizada " + sufixo + "  ",
                        "  Texto atualizado de teste.  ",
                        240,
                        (short) 2026,
                        artistaNovo.getIdArtista(),
                        Set.of(participanteNovo.getIdArtista()),
                        albumNovo.getIdAlbum(),
                        Set.of("  " + generoNovo.getNome() + "  ")
                )
        );

        entityManager.flush();
        entityManager.clear();

        Musica musicaAtualizada = musicaRepository
                .findById(idMusica)
                .orElseThrow();

        assertThat(response.id()).isEqualTo(idMusica);
        assertThat(response.titulo())
                .isEqualTo("Música atualizada " + sufixo);
        assertThat(response.letra())
                .isEqualTo("Texto atualizado de teste.");
        assertThat(response.duracaoSegundos()).isEqualTo(240);
        assertThat(response.anoLancamento()).isEqualTo((short) 2026);
        assertThat(musicaAtualizada.getIdMusica()).isEqualTo(idMusica);
        assertThat(musicaAtualizada.getArtistaPrincipal().getIdArtista())
                .isEqualTo(artistaNovo.getIdArtista());
        assertThat(musicaAtualizada.getAlbum().getIdAlbum())
                .isEqualTo(albumNovo.getIdAlbum());
        assertThat(musicaAtualizada.getArtistasParticipantes())
                .extracting(Artista::getIdArtista)
                .containsExactly(participanteNovo.getIdArtista());
        assertThat(musicaAtualizada.getGeneros())
                .extracting(Genero::getIdGenero)
                .containsExactly(generoNovo.getIdGenero());
        assertThat(musicaRepository.count()).isEqualTo(quantidadeAntes);
        assertThat(contarMusicaNaPlaylist(
                playlist.getId(),
                idMusica
        )).isEqualTo(1L);
    }

    @Test
    void deveExcluirMusicaELimparTodasAsDependencias() {
        String sufixo = sufixoUnico();
        Artista artista = salvarArtista("Artista exclusão " + sufixo);
        Artista participante = salvarArtista(
                "Participante exclusão " + sufixo
        );
        Album album = salvarAlbum(artista, "Álbum exclusão " + sufixo);
        Genero genero = generoRepository.saveAndFlush(
                new Genero("Gênero exclusão " + sufixo)
        );
        Musica musica = new Musica(
                "Música exclusão " + sufixo,
                null,
                200,
                (short) 2025,
                artista,
                album
        );
        musica.setArtistasParticipantes(Set.of(participante));
        musica.setGeneros(Set.of(genero));
        musica = musicaRepository.saveAndFlush(musica);

        Long idMusica = musica.getIdMusica();
        Usuario usuario = salvarUsuario("exclusao-musica-" + sufixo);
        Playlist playlist = salvarPlaylistComMusica(usuario, musica);
        Perfil perfil = perfilRepository.saveAndFlush(new Perfil(usuario));

        adicionarDependenciasDeInteracao(
                usuario.getId(),
                idMusica,
                perfil.getId()
        );

        entityManager.flush();
        entityManager.clear();

        musicaService.excluirMusica(idMusica);
        musicaRepository.flush();
        entityManager.clear();

        assertThat(musicaRepository.existsById(idMusica)).isFalse();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM musica_artista WHERE id_musica = ?",
                Long.class,
                idMusica
        )).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM musica_genero WHERE id_musica = ?",
                Long.class,
                idMusica
        )).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM playlist_musica WHERE id_musica = ?",
                Long.class,
                idMusica
        )).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review WHERE id_musica = ?",
                Long.class,
                idMusica
        )).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM curtida_musica WHERE id_musica = ?",
                Long.class,
                idMusica
        )).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reproducao WHERE id_musica = ?",
                Long.class,
                idMusica
        )).isZero();
        assertThat(buscarMusicaDestaque(perfil.getId())).isNull();
        assertThat(playlistRepository.existsById(playlist.getId())).isTrue();
        assertThat(artistaRepository.existsById(
                artista.getIdArtista()
        )).isTrue();
        assertThat(artistaRepository.existsById(
                participante.getIdArtista()
        )).isTrue();
        assertThat(albumRepository.existsById(album.getIdAlbum())).isTrue();
        assertThat(generoRepository.existsById(genero.getIdGenero())).isTrue();
    }

    private Artista salvarArtista(String nome) {
        return artistaRepository.saveAndFlush(
                new Artista(
                        nome,
                        nome + " completo",
                        "Descrição de teste.",
                        null
                )
        );
    }

    private Album salvarAlbum(Artista artista, String titulo) {
        return albumRepository.saveAndFlush(
                new Album(
                        artista,
                        titulo,
                        (short) 2025,
                        null
                )
        );
    }

    private Usuario salvarUsuario(String identificador) {
        return usuarioRepository.saveAndFlush(
                new Usuario(
                        "Usuário de teste",
                        identificador + "@teste.com",
                        "senha-de-teste",
                        Role.USER
                )
        );
    }

    private Playlist salvarPlaylistComMusica(
            Usuario usuario,
            Musica musica
    ) {
        Playlist playlist = playlistRepository.saveAndFlush(
                new Playlist(
                        "Playlist de teste",
                        "Validação da associação com música.",
                        usuario
                )
        );
        playlist.adicionarMusica(musica);

        return playlistRepository.saveAndFlush(playlist);
    }

    private void adicionarDependenciasDeInteracao(
            Long idUsuario,
            Long idMusica,
            Long idPerfil
    ) {
        jdbcTemplate.update(
                "UPDATE perfil SET id_musica_destaque = ? "
                        + "WHERE id_perfil = ?",
                idMusica,
                idPerfil
        );
        jdbcTemplate.update(
                "INSERT INTO review "
                        + "(id_usuario, id_musica, nota, texto) "
                        + "VALUES (?, ?, ?, ?)",
                idUsuario,
                idMusica,
                5,
                "Review de teste."
        );
        jdbcTemplate.update(
                "INSERT INTO curtida_musica (id_usuario, id_musica) "
                        + "VALUES (?, ?)",
                idUsuario,
                idMusica
        );
        jdbcTemplate.update(
                "INSERT INTO reproducao "
                        + "(id_usuario, id_musica, segundos_ouvidos) "
                        + "VALUES (?, ?, ?)",
                idUsuario,
                idMusica,
                30
        );
    }

    private Long contarMusicaNaPlaylist(
            Long idPlaylist,
            Long idMusica
    ) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM playlist_musica "
                        + "WHERE id_playlist = ? AND id_musica = ?",
                Long.class,
                idPlaylist,
                idMusica
        );
    }

    private Long buscarMusicaDestaque(Long idPerfil) {
        return jdbcTemplate.queryForObject(
                "SELECT id_musica_destaque FROM perfil "
                        + "WHERE id_perfil = ?",
                Long.class,
                idPerfil
        );
    }

    private String sufixoUnico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
