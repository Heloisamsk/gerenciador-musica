package gerenciador_musica_backend.repository;

import gerenciador_musica_backend.model.CurtidaMusica;
import gerenciador_musica_backend.model.CurtidaMusicaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;

public interface CurtidaMusicaRepository
        extends JpaRepository<CurtidaMusica, CurtidaMusicaId> {

    boolean existsByUsuario_IdAndMusica_IdMusica(
            Long usuarioId,
            Long musicaId
    );

    @Transactional
    void deleteByUsuario_IdAndMusica_IdMusica(
            Long usuarioId,
            Long musicaId
    );

    /*
     * Busca em lote: quais, dentre os ids informados, o usuário já
     * curtiu. Usado para enriquecer páginas de listagem sem cair em
     * N+1 (uma consulta só por página, não uma por música).
     */
    @Query("""
            SELECT c.musica.idMusica
            FROM CurtidaMusica c
            WHERE c.usuario.id = :usuarioId
            AND c.musica.idMusica IN :musicaIds
            """)
    Set<Long> buscarIdsCurtidosPeloUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("musicaIds") Collection<Long> musicaIds
    );
}
