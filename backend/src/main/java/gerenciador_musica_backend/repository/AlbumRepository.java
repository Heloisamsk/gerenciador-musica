package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    boolean existsByArtista_IdArtista(Long idArtista);

    List<Album> findByArtistaIdArtistaOrderByTituloAscAnoLancamentoAsc(
            Long idArtista
    );

    boolean existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamento(
            String titulo,
            Long idArtista,
            Short anoLancamento
    );
}
