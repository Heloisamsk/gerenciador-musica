package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
}
