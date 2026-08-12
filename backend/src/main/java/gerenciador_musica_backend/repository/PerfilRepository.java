package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    Optional<Perfil> findByUsuario_Id(Long usuarioId);
}
