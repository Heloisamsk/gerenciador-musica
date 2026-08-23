package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.exception.AlbumEmUsoException;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AlbumAtualizacaoExclusaoIntegrationTest {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private MusicaRepository musicaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void devePersistirAtualizacaoSemAlterarIdArtistaOuMusicas() {
        String sufixo = sufixoUnico();
        Artista artista = salvarArtista("Artista álbum " + sufixo);
        Album album = albumRepository.saveAndFlush(
                new Album(
                        artista,
                        "Álbum original " + sufixo,
                        (short) 2025,
                        "https://example.com/capa-original.jpg"
                )
        );
        Musica musica = musicaRepository.saveAndFlush(
                novaMusica(
                        "Música associada " + sufixo,
                        artista,
                        album
                )
        );
        Long idAlbum = album.getIdAlbum();
        Long idArtista = artista.getIdArtista();
        long quantidadeAlbunsAntes = albumRepository.count();

        AlbumResponseDTO response = albumService.atualizarAlbum(
                idAlbum,
                new AlbumAtualizacaoRequestDTO(
                        "  Álbum   atualizado " + sufixo + "  ",
                        (short) 2026,
                        "  https://example.com/capa-atualizada.jpg  "
                )
        );

        entityManager.flush();
        entityManager.clear();

        Album albumAtualizado = albumRepository
                .findById(idAlbum)
                .orElseThrow();
        Musica musicaAtualizada = musicaRepository
                .findById(musica.getIdMusica())
                .orElseThrow();

        assertThat(response.idAlbum()).isEqualTo(idAlbum);
        assertThat(response.titulo())
                .isEqualTo("Álbum atualizado " + sufixo);
        assertThat(response.anoLancamento()).isEqualTo((short) 2026);
        assertThat(response.capaUrl())
                .isEqualTo("https://example.com/capa-atualizada.jpg");
        assertThat(albumAtualizado.getIdAlbum()).isEqualTo(idAlbum);
        assertThat(albumAtualizado.getArtista().getIdArtista())
                .isEqualTo(idArtista);
        assertThat(musicaAtualizada.getAlbum().getIdAlbum())
                .isEqualTo(idAlbum);
        assertThat(albumRepository.count())
                .isEqualTo(quantidadeAlbunsAntes);
    }

    @Test
    void deveExcluirAlbumLivreELimparDestaqueDoPerfil() {
        String sufixo = sufixoUnico();
        Artista artista = salvarArtista("Artista livre " + sufixo);
        Album album = albumRepository.saveAndFlush(
                new Album(
                        artista,
                        "Álbum livre " + sufixo,
                        (short) 2026,
                        null
                )
        );
        Perfil perfil = salvarPerfilComDestaque(album);
        Long idAlbum = album.getIdAlbum();
        Long idArtista = artista.getIdArtista();

        albumService.excluirAlbum(idAlbum);
        albumRepository.flush();

        assertThat(albumRepository.existsById(idAlbum)).isFalse();
        assertThat(artistaRepository.existsById(idArtista)).isTrue();
        assertThat(perfilRepository.existsById(perfil.getId())).isTrue();
        assertThat(buscarAlbumDestaque(perfil.getId())).isNull();
    }

    @Test
    void naoDeveExcluirAlbumNemMusicaAssociada() {
        String sufixo = sufixoUnico();
        Artista artista = salvarArtista("Artista em uso " + sufixo);
        Album album = albumRepository.saveAndFlush(
                new Album(
                        artista,
                        "Álbum em uso " + sufixo,
                        (short) 2026,
                        null
                )
        );
        Musica musica = musicaRepository.saveAndFlush(
                novaMusica(
                        "Música impeditiva " + sufixo,
                        artista,
                        album
                )
        );

        assertThatThrownBy(() -> albumService.excluirAlbum(
                album.getIdAlbum()
        ))
                .isInstanceOf(AlbumEmUsoException.class)
                .hasMessage(
                        "Não é possível excluir o álbum porque "
                                + "ele possui músicas associadas."
                );

        assertThat(albumRepository.existsById(
                album.getIdAlbum()
        )).isTrue();
        assertThat(musicaRepository.existsById(
                musica.getIdMusica()
        )).isTrue();
        assertThat(artistaRepository.existsById(
                artista.getIdArtista()
        )).isTrue();
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

    private Musica novaMusica(
            String titulo,
            Artista artista,
            Album album
    ) {
        return new Musica(
                titulo,
                null,
                180,
                (short) 2026,
                artista,
                album
        );
    }

    private Perfil salvarPerfilComDestaque(Album album) {
        String sufixo = sufixoUnico();
        Usuario usuario = usuarioRepository.saveAndFlush(
                new Usuario(
                        "Usuário de teste",
                        "album-perfil-" + sufixo + "@teste.com",
                        "senha-de-teste",
                        Role.USER
                )
        );
        Perfil perfil = perfilRepository.saveAndFlush(
                new Perfil(usuario)
        );

        jdbcTemplate.update(
                "UPDATE perfil SET id_album_destaque = ? "
                        + "WHERE id_perfil = ?",
                album.getIdAlbum(),
                perfil.getId()
        );

        return perfil;
    }

    private Long buscarAlbumDestaque(Long idPerfil) {
        return jdbcTemplate.queryForObject(
                "SELECT id_album_destaque FROM perfil "
                        + "WHERE id_perfil = ?",
                Long.class,
                idPerfil
        );
    }

    private String sufixoUnico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
