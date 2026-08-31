package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.repository.projection.AlbumCatalogoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    boolean existsByArtista_IdArtista(Long idArtista);

    List<Album> findByArtistaIdArtistaOrderByTituloAscAnoLancamentoAsc(
            Long idArtista
    );

    List<Album> findByCapaUrlIsNotNull();

    boolean existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamento(
            String titulo,
            Long idArtista,
            Short anoLancamento
    );

    boolean existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamentoAndIdAlbumNot(
            String titulo,
            Long idArtista,
            Short anoLancamento,
            Long idAlbum
    );

    @Query(value = """
            SELECT
                id_album AS "idAlbum",
                id_artista AS "idArtista",
                nome_artista AS "nomeArtista",
                titulo,
                ano_lancamento AS "anoLancamento",
                capa_url AS "capaUrl",
                total_musicas AS "totalMusicas",
                duracao_total_segundos AS "duracaoTotalSegundos"
            FROM vw_albuns_artista_catalogo
            WHERE id_artista = :idArtista
            ORDER BY ano_lancamento DESC, titulo ASC, id_album ASC
            """, nativeQuery = true)
    List<AlbumCatalogoProjection> buscarCatalogoPorArtista(
            @Param("idArtista") Long idArtista
    );

    @Query(value = """
            SELECT
                id_album AS "idAlbum",
                id_artista AS "idArtista",
                nome_artista AS "nomeArtista",
                titulo,
                ano_lancamento AS "anoLancamento",
                capa_url AS "capaUrl",
                total_musicas AS "totalMusicas",
                duracao_total_segundos AS "duracaoTotalSegundos"
            FROM vw_albuns_artista_catalogo
            WHERE id_album = :idAlbum
            """, nativeQuery = true)
    Optional<AlbumCatalogoProjection> buscarCatalogoPorId(
            @Param("idAlbum") Long idAlbum
    );
}
