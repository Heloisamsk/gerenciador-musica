package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.repository.projection.MusicaCatalogoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MusicaRepository
        extends JpaRepository<Musica, Long>,
        JpaSpecificationExecutor<Musica> {

    boolean existsByArtistaPrincipal_IdArtista(Long idArtista);

    boolean existsByArtistasParticipantes_IdArtista(Long idArtista);

    boolean existsByAlbum_IdAlbum(Long idAlbum);

    boolean existsByAlbumAndTituloIgnoreCase(
            Album album,
            String titulo
    );

    boolean existsByAlbumAndTituloIgnoreCaseAndIdMusicaNot(
            Album album,
            String titulo,
            Long idMusica
    );

    boolean existsByAlbumIsNullAndArtistaPrincipalAndTituloIgnoreCaseAndAnoLancamento(
            Artista artistaPrincipal,
            String titulo,
            Short anoLancamento
    );

    boolean existsByAlbumIsNullAndArtistaPrincipalAndTituloIgnoreCaseAndAnoLancamentoAndIdMusicaNot(
            Artista artistaPrincipal,
            String titulo,
            Short anoLancamento,
            Long idMusica
    );

    @Query(value = """
            SELECT
                id_musica AS "idMusica",
                titulo,
                duracao_segundos AS "duracaoSegundos",
                ano_lancamento AS "anoLancamento",
                id_artista_principal AS "idArtistaPrincipal",
                nome_artista_principal AS "nomeArtistaPrincipal",
                id_album AS "idAlbum",
                titulo_album AS "tituloAlbum",
                capa_url AS "capaUrl",
                generos,
                papel_artista AS "papelArtista"
            FROM vw_musicas_artista_catalogo
            WHERE id_artista_contexto = :idArtista
            ORDER BY ano_lancamento DESC, titulo ASC, id_musica ASC
            """, nativeQuery = true)
    List<MusicaCatalogoProjection> buscarCatalogoPorArtista(
            @Param("idArtista") Long idArtista
    );
}
