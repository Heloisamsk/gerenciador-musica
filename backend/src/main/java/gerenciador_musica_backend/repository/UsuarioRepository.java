package gerenciador_musica_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import gerenciador_musica_backend.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(
            String username,
            Long usuarioId
    );
    
}
