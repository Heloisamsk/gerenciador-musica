package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.ArtistaRequestDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import jakarta.persistence.EntityManager;
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
class ArtistaAtualizacaoIntegrationTest {

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private MusicaRepository musicaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void devePersistirAtualizacaoSemAlterarIdOuAssociacoes() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Artista artista = salvarArtista(
                "Artista original " + sufixo
        );
        Artista outroArtista = salvarArtista(
                "Outro artista " + sufixo
        );
        Long idArtista = artista.getIdArtista();
        long quantidadeArtistasAntes = artistaRepository.count();

        Album album = albumRepository.saveAndFlush(
                new Album(
                        artista,
                        "Álbum associado " + sufixo,
                        (short) 2026,
                        null
                )
        );
        Musica musicaPrincipal = musicaRepository.saveAndFlush(
                novaMusica(
                        "Música principal " + sufixo,
                        artista,
                        album
                )
        );
        Musica musicaComParticipacao = novaMusica(
                "Música com participação " + sufixo,
                outroArtista,
                null
        );
        musicaComParticipacao.setArtistasParticipantes(Set.of(artista));
        musicaComParticipacao = musicaRepository.saveAndFlush(
                musicaComParticipacao
        );

        ArtistaRequestDTO request = new ArtistaRequestDTO(
                "Artista atualizado " + sufixo,
                "Nome completo atualizado",
                "Descrição atualizada e persistida.",
                "https://exemplo.com/artista-atualizado.jpg"
        );

        ArtistaResponseDTO response = artistaService.atualizarArtista(
                idArtista,
                request
        );

        entityManager.flush();
        entityManager.clear();

        Artista artistaAtualizado = artistaRepository
                .findById(idArtista)
                .orElseThrow();
        Album albumAtualizado = albumRepository
                .findById(album.getIdAlbum())
                .orElseThrow();
        Musica musicaPrincipalAtualizada = musicaRepository
                .findById(musicaPrincipal.getIdMusica())
                .orElseThrow();
        Musica participacaoAtualizada = musicaRepository
                .findById(musicaComParticipacao.getIdMusica())
                .orElseThrow();

        assertThat(response.idArtista()).isEqualTo(idArtista);
        assertThat(artistaAtualizado.getIdArtista()).isEqualTo(idArtista);
        assertThat(artistaAtualizado.getNome())
                .isEqualTo("Artista atualizado " + sufixo);
        assertThat(artistaAtualizado.getNomeCompleto())
                .isEqualTo("Nome completo atualizado");
        assertThat(artistaAtualizado.getDescricao())
                .isEqualTo("Descrição atualizada e persistida.");
        assertThat(artistaAtualizado.getFotoPerfilUrl())
                .isEqualTo(
                        "https://exemplo.com/artista-atualizado.jpg"
                );
        assertThat(artistaRepository.count())
                .isEqualTo(quantidadeArtistasAntes);
        assertThat(albumAtualizado.getArtista().getIdArtista())
                .isEqualTo(idArtista);
        assertThat(musicaPrincipalAtualizada
                .getArtistaPrincipal()
                .getIdArtista())
                .isEqualTo(idArtista);
        assertThat(participacaoAtualizada.getArtistasParticipantes())
                .extracting(Artista::getIdArtista)
                .contains(idArtista);
    }

    private Artista salvarArtista(String nome) {
        return artistaRepository.saveAndFlush(
                new Artista(
                        nome,
                        nome + " completo",
                        "Descrição original.",
                        "https://exemplo.com/artista-original.jpg"
                )
        );
    }

    private Musica novaMusica(
            String titulo,
            Artista artistaPrincipal,
            Album album
    ) {
        return new Musica(
                titulo,
                null,
                180,
                (short) 2026,
                artistaPrincipal,
                album
        );
    }
}
