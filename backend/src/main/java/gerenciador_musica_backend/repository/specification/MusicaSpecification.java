package gerenciador_musica_backend.repository.specification;

import gerenciador_musica_backend.dto.MusicaFiltroDTO;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Genero;
import gerenciador_musica_backend.model.Musica;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

/**
 * Monta a consulta dinâmica de pesquisa de músicas (US06) combinando, com AND,
 * apenas os filtros que foram efetivamente informados em {@link MusicaFiltroDTO}.
 */
public class MusicaSpecification {

    private MusicaSpecification() {
    }

    public static Specification<Musica> comFiltros(MusicaFiltroDTO filtro) {
        Specification<Musica> resultado = distinct();

        resultado = and(resultado, porTitulo(filtro.titulo()));
        resultado = and(resultado, porArtista(filtro.artistaId()));
        resultado = and(resultado, porAlbum(filtro.albumId()));
        resultado = and(resultado, porGenero(filtro.generoId()));
        resultado = and(resultado, porAno(filtro.anoLancamento()));

        return resultado;
    }

    private static Specification<Musica> porTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return null;
        }

        String termoNormalizado = titulo.trim().toLowerCase(Locale.ROOT);

        return (root, query, cb) -> cb.like(
                cb.lower(root.get("titulo")),
                "%" + termoNormalizado + "%"
        );
    }

    private static Specification<Musica> porArtista(Long artistaId) {
        if (artistaId == null) {
            return null;
        }

        return (root, query, cb) -> {
            Join<Musica, Artista> participantes = root.join(
                    "artistasParticipantes",
                    JoinType.LEFT
            );

            return cb.or(
                    cb.equal(root.get("artistaPrincipal").get("idArtista"), artistaId),
                    cb.equal(participantes.get("idArtista"), artistaId)
            );
        };
    }

    private static Specification<Musica> porAlbum(Long albumId) {
        if (albumId == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(
                root.get("album").get("idAlbum"),
                albumId
        );
    }

    private static Specification<Musica> porGenero(Long generoId) {
        if (generoId == null) {
            return null;
        }

        return (root, query, cb) -> {
            Join<Musica, Genero> generos = root.join("generos", JoinType.LEFT);
            return cb.equal(generos.get("idGenero"), generoId);
        };
    }

    private static Specification<Musica> porAno(Short anoLancamento) {
        if (anoLancamento == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(
                root.get("anoLancamento"),
                anoLancamento
        );
    }

    private static Specification<Musica> distinct() {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.conjunction();
        };
    }

    private static Specification<Musica> and(
            Specification<Musica> base,
            Specification<Musica> novoFiltro
    ) {
        if (novoFiltro == null) {
            return base;
        }

        return base == null ? novoFiltro : base.and(novoFiltro);
    }
}
