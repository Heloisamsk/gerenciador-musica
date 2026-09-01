package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.CurtidaAlbum;
import gerenciador_musica_backend.model.CurtidaAlbumId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CurtidaAlbumRepository
        extends JpaRepository<CurtidaAlbum, CurtidaAlbumId> {

    boolean existsByUsuario_IdAndAlbum_IdAlbum(
            Long usuarioId,
            Long albumId
    );

    @Transactional
    void deleteByUsuario_IdAndAlbum_IdAlbum(
            Long usuarioId,
            Long albumId
    );

    /*
     * Busca em lote: quais, dentre os ids informados, o usuário já
     * curtiu. Usado para enriquecer páginas de listagem sem cair em
     * N+1 (uma consulta só por página, não uma por álbum).
     */
    @Query("""
            SELECT c.album.idAlbum
            FROM CurtidaAlbum c
            WHERE c.usuario.id = :usuarioId
            AND c.album.idAlbum IN :albumIds
            """)
    Set<Long> buscarIdsCurtidosPeloUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("albumIds") Collection<Long> albumIds
    );

    @Query("""
            SELECT c.album
            FROM CurtidaAlbum c
            WHERE c.usuario.id = :usuarioId
            ORDER BY c.curtidaEm DESC
            """)
    List<Album> buscarAlbunsCurtidosPeloUsuario(
            @Param("usuarioId") Long usuarioId
    );
}
