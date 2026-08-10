package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Musica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MusicaRepository extends JpaRepository<Musica, Long> {

    boolean existsByTituloIgnoreCaseAndAlbum_IdAlbum(
            String titulo,
            Long idAlbum
    );

    boolean existsByTituloIgnoreCaseAndAlbumIsNullAndAnoLancamentoAndDuracaoSegundos(
            String titulo,
            Short anoLancamento,
            Integer duracaoSegundos
    );

}
