package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.MusicaResumoDTO;
import gerenciador_musica_backend.dto.PlaylistRequestDTO;
import gerenciador_musica_backend.dto.PlaylistResponseDTO;
import gerenciador_musica_backend.exception.MusicaDuplicadaException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.exception.PlaylistAcessoNegadoException;
import gerenciador_musica_backend.exception.PlaylistEspecialException;
import gerenciador_musica_backend.exception.PlaylistNaoEncontradaException;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Playlist;
import gerenciador_musica_backend.model.PlaylistMusica;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.PlaylistRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaylistService {

    private static final String PLAYLIST_NAO_ENCONTRADA =
            "Playlist não encontrada.";

    private final PlaylistRepository playlistRepository;
    private final MusicaRepository musicaRepository;

    public PlaylistService(
            PlaylistRepository playlistRepository,
            MusicaRepository musicaRepository
    ) {
        this.playlistRepository = playlistRepository;
        this.musicaRepository = musicaRepository;
    }

    @Transactional
    public PlaylistResponseDTO criarPlaylist(PlaylistRequestDTO dto) {
        Usuario usuario = obterUsuarioAutenticado();

        Playlist playlist = new Playlist(
                dto.getNome(),
                dto.getDescricao(),
                usuario
        );
        playlist.setCapaUrl(dto.getCapaUrl());

        Playlist playlistSalva = playlistRepository.save(playlist);

        return converterParaResponseDTO(playlistSalva);
    }

    @Transactional
    public PlaylistResponseDTO atualizarPlaylist(
            Long id,
            PlaylistRequestDTO dto
    ) {
        Usuario usuario = obterUsuarioAutenticado();

        Playlist playlist = playlistRepository.buscarComMusicasPorId(id)
                .orElseThrow(() ->
                        new PlaylistNaoEncontradaException(
                                PLAYLIST_NAO_ENCONTRADA
                        )
                );

        verificarProprietario(playlist, usuario);
        verificarNaoEspecial(playlist);

        playlist.setNome(dto.getNome());
        playlist.setDescricao(dto.getDescricao());
        playlist.setCapaUrl(dto.getCapaUrl());

        Playlist playlistSalva = playlistRepository.save(playlist);

        return converterParaResponseDTO(playlistSalva);
    }

    @Transactional
    public void excluirPlaylist(Long id) {
        Usuario usuario = obterUsuarioAutenticado();

        Playlist playlist = playlistRepository.buscarComMusicasPorId(id)
                .orElseThrow(() ->
                        new PlaylistNaoEncontradaException(
                                PLAYLIST_NAO_ENCONTRADA
                        )
                );

        verificarProprietario(playlist, usuario);
        verificarNaoEspecial(playlist);

        playlistRepository.delete(playlist);
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponseDTO> listarMinhasPlaylists() {
        Usuario usuario = obterUsuarioAutenticado();

        return playlistRepository
                .findByUsuarioIdOrderByDataCriacaoDesc(usuario.getId())
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaylistResponseDTO buscarPlaylist(Long id) {
        Usuario usuario = obterUsuarioAutenticado();

        Playlist playlist = playlistRepository.buscarComMusicasPorId(id)
                .orElseThrow(() ->
                        new PlaylistNaoEncontradaException(
                                PLAYLIST_NAO_ENCONTRADA
                        )
                );

        verificarProprietario(playlist, usuario);

        return converterParaResponseDTO(playlist);
    }

    @Transactional
    public void adicionarMusica(Long playlistId, Long musicaId) {
        Usuario usuario = obterUsuarioAutenticado();

        Playlist playlist = playlistRepository
                .buscarComMusicasPorId(playlistId)
                .orElseThrow(() ->
                        new PlaylistNaoEncontradaException(
                                PLAYLIST_NAO_ENCONTRADA
                        )
                );

        verificarProprietario(playlist, usuario);
        verificarNaoEspecial(playlist);

        Musica musica = musicaRepository.findById(musicaId)
                .orElseThrow(() ->
                        new MusicaNaoEncontradaException(musicaId)
                );

        boolean jaExiste = playlist.getMusicas()
                .stream()
                .map(PlaylistMusica::getMusica)
                .anyMatch(item ->
                        item.getIdMusica().equals(musicaId)
                );

        if (jaExiste) {
            throw new MusicaDuplicadaException(
                    "Esta música já está na playlist."
            );
        }

        playlist.adicionarMusica(musica);
        playlistRepository.save(playlist);
    }

    @Transactional
    public void removerMusica(Long playlistId, Long musicaId) {
        Usuario usuario = obterUsuarioAutenticado();

        Playlist playlist = playlistRepository
                .buscarComMusicasPorId(playlistId)
                .orElseThrow(() ->
                        new PlaylistNaoEncontradaException(
                                PLAYLIST_NAO_ENCONTRADA
                        )
                );

        verificarProprietario(playlist, usuario);
        verificarNaoEspecial(playlist);

        boolean removida = playlist.removerMusica(musicaId);

        if (!removida) {
            throw new MusicaNaoEncontradaException(musicaId);
        }

        playlistRepository.save(playlist);
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

    private void verificarProprietario(
            Playlist playlist,
            Usuario usuario
    ) {
        if (!playlist.getUsuario().getId().equals(usuario.getId())) {
            throw new PlaylistAcessoNegadoException(
                    "Você não possui permissão para acessar esta playlist."
            );
        }
    }

    /*
     * A playlist "Favoritos" é gerenciada automaticamente a partir das
     * curtidas (ver CurtidaService) — nome, capa e as músicas dela não
     * podem ser alteradas por aqui, nem ela pode ser excluída.
     */
    private void verificarNaoEspecial(Playlist playlist) {
        if (playlist.isEspecial()) {
            throw new PlaylistEspecialException(
                    "A playlist Favoritos é gerenciada automaticamente "
                            + "pelas curtidas e não pode ser editada, "
                            + "excluída ou ter músicas adicionadas/"
                            + "removidas diretamente."
            );
        }
    }

    private PlaylistResponseDTO converterParaResponseDTO(
            Playlist playlist
    ) {
        List<MusicaResumoDTO> musicas = playlist.getMusicas()
                .stream()
                .map(PlaylistMusica::getMusica)
                .map(musica -> new MusicaResumoDTO(
                        musica.getIdMusica(),
                        musica.getTitulo(),
                        musica.getArtistaPrincipal() != null
                                ? musica.getArtistaPrincipal().getNome()
                                : null,
                        musica.getAlbum() != null
                                ? musica.getAlbum().getCapaUrl()
                                : null
                ))
                .toList();

        PlaylistResponseDTO response = new PlaylistResponseDTO();
        response.setId(playlist.getId());
        response.setNome(playlist.getNome());
        response.setDescricao(playlist.getDescricao());
        response.setCapaUrl(playlist.getCapaUrl());
        response.setMusicas(musicas);
        response.setEspecial(playlist.isEspecial());

        return response;
    }
}
