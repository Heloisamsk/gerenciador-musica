package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Artista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistaRepository extends JpaRepository<Artista, Long> {

    Optional<Artista> findByNomeIgnoreCase(String nome);

    Optional<Artista> findById(Long idArtista);

    boolean existsByNomeIgnoreCase(String nome);
}
