package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUsuario_IdAndMusica_IdMusica(
            Long idUsuario,
            Long idMusica
    );

    boolean existsByUsuario_IdAndAlbum_IdAlbum(
            Long idUsuario,
            Long idAlbum
    );

    Page<Review> findAllByOrderByCriadaEmDesc(Pageable pageable);

    Page<Review> findByUsuario_IdOrderByCriadaEmDesc(
            Long idUsuario,
            Pageable pageable
    );

    Page<Review> findByUsuario_IdInOrderByCriadaEmDesc(
            List<Long> idsUsuarios,
            Pageable pageable
    );

    Page<Review> findByMusica_IdMusicaOrderByCriadaEmDesc(
            Long idMusica,
            Pageable pageable
    );

    Page<Review> findByAlbum_IdAlbumOrderByCriadaEmDesc(
            Long idAlbum,
            Pageable pageable
    );

    long countByUsuario_IdAndMusicaIsNotNull(Long idUsuario);

    long countByUsuario_IdAndAlbumIsNotNull(Long idUsuario);
}