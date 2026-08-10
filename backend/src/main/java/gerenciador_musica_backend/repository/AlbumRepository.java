package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findByTituloIgnoreCaseAndArtistaAndAnoLancamento(
            String titulo,
            Artista artista,
            Short anoLancamento
    );
}
