package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.repository.projection.ArtistaCatalogoResumoProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtistaRepository extends JpaRepository<Artista, Long> {

    Optional<Artista> findByNomeIgnoreCase(String nome);

    Optional<Artista> findById(Long idArtista);

    List<Artista> findByNomeContainingIgnoreCaseOrNomeCompletoContainingIgnoreCase(
            String nome,
            String nomeCompleto,
            Pageable pageable
    );

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdArtistaNot(
            String nome,
            Long idArtista
    );

    @Query(value = """
            SELECT
                id_artista AS "idArtista",
                nome,
                nome_completo AS "nomeCompleto",
                descricao,
                foto_perfil_url AS "fotoPerfilUrl",
                total_albuns AS "totalAlbuns",
                total_musicas_principais AS "totalMusicasPrincipais",
                total_participacoes AS "totalParticipacoes",
                duracao_total_segundos AS "duracaoTotalSegundos"
            FROM vw_artista_resumo_catalogo
            WHERE id_artista = :idArtista
            """, nativeQuery = true)
    Optional<ArtistaCatalogoResumoProjection> buscarResumoCatalogo(
            @Param("idArtista") Long idArtista
    );
}
