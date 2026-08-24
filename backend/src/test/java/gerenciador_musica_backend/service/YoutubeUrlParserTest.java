package gerenciador_musica_backend.service;

import gerenciador_musica_backend.exception.DadosMusicaInvalidosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YoutubeUrlParserTest {

    private static final String VIDEO_ID = "dQw4w9WgXcQ";

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?t=30",
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://youtube.com/shorts/dQw4w9WgXcQ",
            "https://music.youtube.com/watch?list=teste&v=dQw4w9WgXcQ",
            "http://m.youtube.com/watch?v=dQw4w9WgXcQ"
    })
    void deveExtrairIdDeFormatosPermitidos(String url) {
        assertThat(YoutubeUrlParser.extrairVideoId(url))
                .isEqualTo(VIDEO_ID);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void deveAceitarLinkNaoInformado(String url) {
        assertThat(YoutubeUrlParser.extrairVideoId(url)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com/watch?v=dQw4w9WgXcQ",
            "javascript:alert(1)",
            "https://youtube.com/watch?v=curto",
            "https://youtube.com/channel/dQw4w9WgXcQ",
            "youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtube.com/watch?v=%"
    })
    void deveRejeitarLinksInvalidosOuDeOutroDominio(String url) {
        assertThatThrownBy(() -> YoutubeUrlParser.extrairVideoId(url))
                .isInstanceOf(DadosMusicaInvalidosException.class)
                .hasMessage("Informe um link válido de vídeo do YouTube.");
    }

    @Test
    void deveIgnorarParametrosAdicionaisDoLinkCurto() {
        String url = "https://youtu.be/dQw4w9WgXcQ?si=exemplo";

        assertThat(YoutubeUrlParser.extrairVideoId(url))
                .isEqualTo(VIDEO_ID);
    }
}
