package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Genero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeneroRepository extends JpaRepository<Genero, Long> {

    Optional<Genero> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}
