package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    //Buscar playlists do usuário:
    List<Playlist> findByUsuarioId(Long usuarioId);


    //Buscar uma playlist específica do usuário:
    Optional<Playlist> findByIdAndUsuarioId(Long id, Long usuarioId);
}