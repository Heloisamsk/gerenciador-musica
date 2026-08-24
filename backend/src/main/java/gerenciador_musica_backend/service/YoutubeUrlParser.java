package gerenciador_musica_backend.service;

import gerenciador_musica_backend.exception.DadosMusicaInvalidosException;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Converte links conhecidos do YouTube em um identificador seguro de vídeo.
 * O sistema persiste apenas o identificador, nunca uma URL arbitrária.
 */
public final class YoutubeUrlParser {

    private static final Pattern VIDEO_ID_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]{11}$");

    private static final Set<String> YOUTUBE_HOSTS = Set.of(
            "youtube.com",
            "www.youtube.com",
            "m.youtube.com",
            "music.youtube.com",
            "youtube-nocookie.com",
            "www.youtube-nocookie.com"
    );

    private static final Set<String> SHORT_LINK_HOSTS = Set.of(
            "youtu.be",
            "www.youtu.be"
    );

    private static final Set<String> PATH_PREFIXES = Set.of(
            "embed",
            "shorts",
            "live"
    );

    private YoutubeUrlParser() {
        // Classe utilitária.
    }

    public static String extrairVideoId(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(youtubeUrl.trim());
            validarEsquema(uri);

            String host = obterHost(uri);
            String videoId;

            if (SHORT_LINK_HOSTS.contains(host)) {
                videoId = primeiroSegmento(uri.getPath());
            } else if (YOUTUBE_HOSTS.contains(host)) {
                videoId = extrairDoYoutube(uri);
            } else {
                throw urlInvalida();
            }

            return validarVideoId(videoId);
        } catch (IllegalArgumentException exception) {
            throw urlInvalida();
        }
    }

    private static void validarEsquema(URI uri) {
        String esquema = uri.getScheme();

        if (
                esquema == null
                || !(esquema.equalsIgnoreCase("https")
                || esquema.equalsIgnoreCase("http"))
        ) {
            throw urlInvalida();
        }
    }

    private static String obterHost(URI uri) {
        String host = uri.getHost();

        if (host == null) {
            throw urlInvalida();
        }

        return host.toLowerCase(Locale.ROOT);
    }

    private static String extrairDoYoutube(URI uri) {
        String[] segmentos = segmentosDoCaminho(uri.getPath());

        if (segmentos.length == 1 && segmentos[0].equals("watch")) {
            return buscarParametro(uri.getRawQuery(), "v");
        }

        if (
                segmentos.length == 2
                && PATH_PREFIXES.contains(segmentos[0])
        ) {
            return segmentos[1];
        }

        throw urlInvalida();
    }

    private static String primeiroSegmento(String caminho) {
        String[] segmentos = segmentosDoCaminho(caminho);

        if (segmentos.length == 0) {
            throw urlInvalida();
        }

        return segmentos[0];
    }

    private static String[] segmentosDoCaminho(String caminho) {
        if (caminho == null) {
            return new String[0];
        }

        return Arrays.stream(caminho.split("/"))
                .filter(segmento -> !segmento.isBlank())
                .toArray(String[]::new);
    }

    private static String buscarParametro(
            String query,
            String nomeEsperado
    ) {
        if (query == null || query.isBlank()) {
            throw urlInvalida();
        }

        return Arrays.stream(query.split("&"))
                .map(parametro -> parametro.split("=", 2))
                .filter(partes -> partes.length == 2)
                .filter(partes -> decodificar(partes[0])
                        .equals(nomeEsperado))
                .map(partes -> decodificar(partes[1]))
                .findFirst()
                .orElseThrow(YoutubeUrlParser::urlInvalida);
    }

    private static String decodificar(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }

    private static String validarVideoId(String videoId) {
        if (
                videoId == null
                || !VIDEO_ID_PATTERN.matcher(videoId).matches()
        ) {
            throw urlInvalida();
        }

        return videoId;
    }

    private static DadosMusicaInvalidosException urlInvalida() {
        return new DadosMusicaInvalidosException(
                "Informe um link válido de vídeo do YouTube."
        );
    }
}
