package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumCatalogoDTO;
import gerenciador_musica_backend.dto.MusicaCatalogoDTO;
import gerenciador_musica_backend.repository.projection.AlbumCatalogoProjection;
import gerenciador_musica_backend.repository.projection.MusicaCatalogoProjection;

import java.util.Arrays;
import java.util.List;

final class CatalogoProjectionMapper {

    private CatalogoProjectionMapper() {
    }

    static AlbumCatalogoDTO converterAlbum(
            AlbumCatalogoProjection album,
            boolean curtida
    ) {
        return new AlbumCatalogoDTO(
                album.getIdAlbum(),
                album.getIdArtista(),
                album.getNomeArtista(),
                album.getTitulo(),
                album.getAnoLancamento(),
                album.getCapaUrl(),
                album.getTotalMusicas(),
                album.getDuracaoTotalSegundos(),
                curtida
        );
    }

    static MusicaCatalogoDTO converterMusica(
            MusicaCatalogoProjection musica
    ) {
        return new MusicaCatalogoDTO(
                musica.getIdMusica(),
                musica.getTitulo(),
                musica.getDuracaoSegundos(),
                musica.getAnoLancamento(),
                musica.getIdArtistaPrincipal(),
                musica.getNomeArtistaPrincipal(),
                musica.getIdAlbum(),
                musica.getTituloAlbum(),
                musica.getCapaUrl(),
                separarGeneros(musica.getGeneros()),
                musica.getPapelArtista()
        );
    }

    static List<String> separarGeneros(String generos) {
        if (generos == null || generos.isBlank()) {
            return List.of();
        }

        return Arrays.stream(generos.split(","))
                .map(String::strip)
                .filter(genero -> !genero.isBlank())
                .toList();
    }
}
