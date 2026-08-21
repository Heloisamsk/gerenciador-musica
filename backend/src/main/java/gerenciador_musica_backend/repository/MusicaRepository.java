package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MusicaRepository extends JpaRepository<Musica, Long>, JpaSpecificationExecutor<Musica> {

    boolean existsByAlbumAndTituloIgnoreCase(
            Album album,
            String titulo
    );

    boolean existsByAlbumIsNullAndArtistaPrincipalAndTituloIgnoreCaseAndAnoLancamento(
            Artista artistaPrincipal,
            String titulo,
            Short anoLancamento
    );
}
