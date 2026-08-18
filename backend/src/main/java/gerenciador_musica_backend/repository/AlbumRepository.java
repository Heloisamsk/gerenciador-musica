package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    //Permitir busca separada
    List<Album> findByTituloContainingIgnoreCase(String titulo);

    List<Album> findByArtistaIdArtista(Long idArtista);

    List<Album> findByAnoLancamento(Short anoLancamento);

    Optional<Album> findByTituloIgnoreCaseAndArtistaAndAnoLancamento(
            String titulo,
            Artista artista,
            Short anoLancamento
    );
    //verfica se existe album duplicado
    boolean existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamento(
            String titulo,
            Long idArtista,
            Short anoLancamento
    );
}


