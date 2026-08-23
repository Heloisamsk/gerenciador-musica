package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MusicaRepository
        extends JpaRepository<Musica, Long>,
        JpaSpecificationExecutor<Musica> {

    boolean existsByArtistaPrincipal_IdArtista(Long idArtista);

    boolean existsByArtistasParticipantes_IdArtista(Long idArtista);

    boolean existsByAlbum_IdAlbum(Long idAlbum);

    boolean existsByAlbumAndTituloIgnoreCase(
            Album album,
            String titulo
    );

    boolean existsByAlbumAndTituloIgnoreCaseAndIdMusicaNot(
            Album album,
            String titulo,
            Long idMusica
    );

    boolean existsByAlbumIsNullAndArtistaPrincipalAndTituloIgnoreCaseAndAnoLancamento(
            Artista artistaPrincipal,
            String titulo,
            Short anoLancamento
    );

    boolean existsByAlbumIsNullAndArtistaPrincipalAndTituloIgnoreCaseAndAnoLancamentoAndIdMusicaNot(
            Artista artistaPrincipal,
            String titulo,
            Short anoLancamento,
            Long idMusica
    );
}
