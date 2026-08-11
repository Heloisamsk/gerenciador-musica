package gerenciador_musica_backend.model;

import java.io.Serializable;
import java.util.Objects;

public class PlaylistMusicaId implements Serializable {

    private Long playlist;
    private Long musica;

    public PlaylistMusicaId() {
    }

    public PlaylistMusicaId(Long playlist, Long musica) {
        this.playlist = playlist;
        this.musica = musica;
    }

    public Long getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Long playlist) {
        this.playlist = playlist;
    }

    public Long getMusica() {
        return musica;
    }

    public void setMusica(Long musica) {
        this.musica = musica;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof PlaylistMusicaId outro)) {
            return false;
        }

        return Objects.equals(playlist, outro.playlist)
                && Objects.equals(musica, outro.musica);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlist, musica);
    }
}
