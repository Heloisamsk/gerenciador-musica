package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.SeguidorArtista;
import gerenciador_musica_backend.model.SeguidorArtistaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

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
}
