package gerenciador_musica_backend.service;

import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.CurtidaAlbum;
import gerenciador_musica_backend.model.CurtidaMusica;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Playlist;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.CurtidaAlbumRepository;
import gerenciador_musica_backend.repository.CurtidaMusicaRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.PlaylistRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurtidaService {

    private static final String NOME_PLAYLIST_FAVORITOS = "Favoritos";

    private final CurtidaMusicaRepository curtidaMusicaRepository;
    private final CurtidaAlbumRepository curtidaAlbumRepository;
    private final PlaylistRepository playlistRepository;
    private final MusicaRepository musicaRepository;
    private final AlbumRepository albumRepository;

    public CurtidaService(
            CurtidaMusicaRepository curtidaMusicaRepository,
            CurtidaAlbumRepository curtidaAlbumRepository,
            PlaylistRepository playlistRepository,
            MusicaRepository musicaRepository,
            AlbumRepository albumRepository
    ) {
        this.curtidaMusicaRepository = curtidaMusicaRepository;
        this.curtidaAlbumRepository = curtidaAlbumRepository;
        this.playlistRepository = playlistRepository;
        this.musicaRepository = musicaRepository;
        this.albumRepository = albumRepository;
    }

    /*
     * Curtir uma música é idempotente: curtir de novo uma música já
     * curtida não faz nada (nem duplica a curtida, nem duplica o item
     * na playlist Favoritos).
     */
    @Transactional
    public void curtirMusica(Long musicaId) {
        Usuario usuario = obterUsuarioAutenticado();

        if (curtidaMusicaRepository.existsByUsuario_IdAndMusica_IdMusica(
                usuario.getId(),
                musicaId
        )) {
            return;
        }

        Musica musica = musicaRepository.findById(musicaId)
                .orElseThrow(() ->
                        new MusicaNaoEncontradaException(musicaId)
                );

        curtidaMusicaRepository.save(
                new CurtidaMusica(usuario, musica)
        );

        Playlist favoritos = obterOuCriarFavoritos(usuario);

        boolean jaNaPlaylist = favoritos.getMusicas()
                .stream()
                .anyMatch(item ->
                        item.getMusica().getIdMusica().equals(musicaId)
                );

        if (!jaNaPlaylist) {
            favoritos.adicionarMusica(musica);
            playlistRepository.save(favoritos);
        }
    }

    @Transactional
    public void descurtirMusica(Long musicaId) {
        Usuario usuario = obterUsuarioAutenticado();

        curtidaMusicaRepository.deleteByUsuario_IdAndMusica_IdMusica(
                usuario.getId(),
                musicaId
        );

        playlistRepository
                .findByUsuarioIdAndEspecialTrue(usuario.getId())
                .ifPresent(favoritos -> {
                    favoritos.removerMusica(musicaId);
                    playlistRepository.save(favoritos);
                });
    }

    @Transactional
    public void curtirAlbum(Long albumId) {
        Usuario usuario = obterUsuarioAutenticado();

        if (curtidaAlbumRepository.existsByUsuario_IdAndAlbum_IdAlbum(
                usuario.getId(),
                albumId
        )) {
            return;
        }

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() ->
                        new AlbumNaoEncontradoException(albumId)
                );

        curtidaAlbumRepository.save(
                new CurtidaAlbum(usuario, album)
        );
    }

    @Transactional
    public void descurtirAlbum(Long albumId) {
        Usuario usuario = obterUsuarioAutenticado();

        curtidaAlbumRepository.deleteByUsuario_IdAndAlbum_IdAlbum(
                usuario.getId(),
                albumId
        );
    }

    private Playlist obterOuCriarFavoritos(Usuario usuario) {
        return playlistRepository
                .findByUsuarioIdAndEspecialTrue(usuario.getId())
                .orElseGet(() -> {
                    Playlist favoritos = new Playlist(
                            NOME_PLAYLIST_FAVORITOS,
                            null,
                            usuario
                    );
                    favoritos.setEspecial(true);

                    return playlistRepository.save(favoritos);
                });
    }

    private Usuario obterUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }

        throw new IllegalStateException(
                "Usuário autenticado não encontrado."
        );
    }
}
