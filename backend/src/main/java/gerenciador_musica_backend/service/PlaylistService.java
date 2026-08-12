package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.MusicaResumoDTO;
import gerenciador_musica_backend.dto.PlaylistRequestDTO;
import gerenciador_musica_backend.dto.PlaylistResponseDTO;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Playlist;
import gerenciador_musica_backend.model.PlaylistMusica;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.PlaylistRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository playlistRepository, UsuarioRepository usuarioRepository) {
        this.playlistRepository = playlistRepository;
    }

    @Transactional
    public PlaylistResponseDTO criarPlaylist(PlaylistRequestDTO dto) {
        Usuario usuario = getUsuarioAutenticado();

        Playlist playlist = new Playlist();
        playlist.setNome(dto.getNome());
        playlist.setDescricao(dto.getDescricao());
        playlist.setUsuario(usuario);

        Playlist playlistSalva = playlistRepository.save(playlist);

        return toResponseDTO(playlistSalva);
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponseDTO> listarMinhas() {
        Usuario usuario = getUsuarioAutenticado();

        return playlistRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlaylistResponseDTO buscarPorId(Long id) {
        Usuario usuario = getUsuarioAutenticado();

        Playlist playlist = playlistRepository
                .findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Playlist não encontrada."
                ));

        return toResponseDTO(playlist);
    }

  private Usuario getUsuarioAutenticado() {
    Object principal = SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

    if (principal instanceof Usuario usuario) {
        return usuario;
    }

    throw new RuntimeException("Usuário autenticado não encontrado no contexto de segurança.");
}

    private PlaylistResponseDTO toResponseDTO(Playlist playlist) {
        List<MusicaResumoDTO> musicas = playlist.getMusicas()
                .stream()
                .map(this::toMusicaResumoDTO)
                .collect(Collectors.toList());

        return new PlaylistResponseDTO(
                playlist.getId(),
                playlist.getNome(),
                playlist.getDescricao(),
                musicas
        );
    }

    private MusicaResumoDTO toMusicaResumoDTO(PlaylistMusica playlistMusica) {
        Musica musica = playlistMusica.getMusica();

        String nomeArtista = musica.getArtistaPrincipal() != null
                ? musica.getArtistaPrincipal().getNome()
                : null;

        return new MusicaResumoDTO(
                musica.getIdMusica(),
                musica.getTitulo(),
                nomeArtista
        );
    }
}