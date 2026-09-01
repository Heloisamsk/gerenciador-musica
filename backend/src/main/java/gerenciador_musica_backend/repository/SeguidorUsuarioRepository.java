package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.SeguidorUsuario;
import gerenciador_musica_backend.model.SeguidorUsuarioId;
import gerenciador_musica_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SeguidorUsuarioRepository
        extends JpaRepository<SeguidorUsuario, SeguidorUsuarioId> {

    boolean existsBySeguidor_IdAndSeguido_Id(
            Long idSeguidor,
            Long idSeguido
    );

    @Transactional
    void deleteBySeguidor_IdAndSeguido_Id(
            Long idSeguidor,
            Long idSeguido
    );

    long countBySeguido_Id(Long idSeguido);

    long countBySeguidor_Id(Long idSeguidor);

    @Query("""
            SELECT s.seguido.id
            FROM SeguidorUsuario s
            WHERE s.seguidor.id = :idSeguidor
            """)
    List<Long> buscarIdsSeguidosPeloUsuario(
            @Param("idSeguidor") Long idSeguidor
    );

    @Query("""
            SELECT s.seguido
            FROM SeguidorUsuario s
            WHERE s.seguidor.id = :idSeguidor
            ORDER BY s.seguidoEm DESC
            """)
    List<Usuario> buscarUsuariosSeguidosPeloUsuario(
            @Param("idSeguidor") Long idSeguidor
    );

    @Query("""
            SELECT s.seguidor
            FROM SeguidorUsuario s
            WHERE s.seguido.id = :idSeguido
            ORDER BY s.seguidoEm DESC
            """)
    List<Usuario> buscarSeguidoresDoUsuario(
            @Param("idSeguido") Long idSeguido
    );
}