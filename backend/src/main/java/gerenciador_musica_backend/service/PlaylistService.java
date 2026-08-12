package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.MusicaResumoDTO;
import gerenciador_musica_backend.dto.PlaylistRequestDTO;
import gerenciador_musica_backend.dto.PlaylistResponseDTO;
import gerenciador_musica_backend.exception.PlaylistAcessoNegadoException;
import gerenciador_musica_backend.exception.PlaylistNaoEncontradaException;
import gerenciador_musica_backend.model.Playlist;
import gerenciador_musica_backend.model.PlaylistMusica;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.PlaylistRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    @Transactional
    public PlaylistResponseDTO criarPlaylist(PlaylistRequestDTO dto) {
        Usuario usuario = obterUsuarioAutenticado();

        Playlist playlist = new Playlist();
        playlist.setNome(dto.getNome());
        playlist.setDescricao(dto.getDescricao());
        playlist.setUsuario(usuario);

        Playlist playlistSalva = playlistRepository.save(playlist);

        return converterParaResponseDTO(playlistSalva);
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponseDTO> listarMinhasPlaylists() {
        Usuario usuario = obterUsuarioAutenticado();

        List<Playlist> playlists =
                playlistRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuario.getId());

        return playlists.stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaylistResponseDTO buscarPlaylist(Long id) {
        Usuario usuario = obterUsuarioAutenticado();

        Playlist playlist = playlistRepository.buscarComMusicasPorId(id)
                .orElseThrow(() -> new PlaylistNaoEncontradaException("Playlist não encontrada."));

        if (!playlist.getUsuario().getId().equals(usuario.getId())) {
            throw new PlaylistAcessoNegadoException(
                    "Você não possui permissão para acessar esta playlist."
            );
        }

        return converterParaResponseDTO(playlist);
    }

    private Usuario obterUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof Usuario usuario) {
            return usuario;
        }

        throw new RuntimeException("Usuário autenticado não encontrado no contexto de segurança.");
    }

    private PlaylistResponseDTO converterParaResponseDTO(Playlist playlist) {
        List<MusicaResumoDTO> musicas = playlist.getMusicas()
                .stream()
                .map(PlaylistMusica::getMusica)
                .map(musica -> new MusicaResumoDTO(
                        musica.getIdMusica(),
                        musica.getTitulo(),
                        musica.getArtistaPrincipal() != null
                                ? musica.getArtistaPrincipal().getNome()
                                : null
                ))
                .toList();

        return new PlaylistResponseDTO(
                playlist.getId(),
                playlist.getNome(),
                playlist.getDescricao(),
                musicas
        );
    }
}