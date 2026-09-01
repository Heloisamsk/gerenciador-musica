package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.SeguidorArtista;
import gerenciador_musica_backend.model.SeguidorArtistaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SeguidorArtistaRepository
        extends JpaRepository<SeguidorArtista, SeguidorArtistaId> {

    boolean existsByUsuario_IdAndArtista_IdArtista(
            Long usuarioId,
            Long artistaId
    );

    @Transactional
    void deleteByUsuario_IdAndArtista_IdArtista(
            Long usuarioId,
            Long artistaId
    );

    @Query("""
            SELECT s.artista
            FROM SeguidorArtista s
            WHERE s.usuario.id = :usuarioId
            ORDER BY s.seguidoEm DESC
            """)
    List<Artista> buscarArtistasSeguidosPeloUsuario(
            @Param("usuarioId") Long usuarioId
    );
}
