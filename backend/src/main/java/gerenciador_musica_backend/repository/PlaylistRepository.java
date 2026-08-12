package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // Listagem leve: sem carregar músicas (evita join desnecessário)
    List<Playlist> findByUsuarioIdOrderByDataCriacaoDesc(Long usuarioId);

    /*
     * Busca por id SEM filtrar por usuário — a checagem de dono
     * é feita no service, pra diferenciar 404 (não existe)
     * de 403 (existe, mas não é sua).
     *
     * JOIN FETCH evita N+1 ao acessar playlist.getMusicas()
     * e cada musica.getArtistaPrincipal().
     */
    @Query("""
            SELECT DISTINCT p
            FROM Playlist p
            LEFT JOIN FETCH p.musicas pm
            LEFT JOIN FETCH pm.musica m
            LEFT JOIN FETCH m.artistaPrincipal
            WHERE p.id = :id
            """)
    Optional<Playlist> buscarComMusicasPorId(@Param("id") Long id);
}