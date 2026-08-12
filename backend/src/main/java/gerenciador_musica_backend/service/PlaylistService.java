package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.PlaylistRequestDTO;
import gerenciador_musica_backend.dto.PlaylistResponseDTO;
import gerenciador_musica_backend.model.Playlist;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.PlaylistRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gerenciador_musica_backend.dto.MusicaResumoDTO;
import gerenciador_musica_backend.exception.PlaylistAcessoNegadoException;
import gerenciador_musica_backend.exception.PlaylistNaoEncontradaException;
import gerenciador_musica_backend.model.PlaylistMusica;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UsuarioRepository usuarioRepository;

    
    public PlaylistService(PlaylistRepository playlistRepository, UsuarioRepository usuarioRepository) {
        this.playlistRepository = playlistRepository;
        this.usuarioRepository = usuarioRepository;
    }
    //pegar o usuário autenticado
    private Usuario obterUsuarioAutenticado() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado."));
    }

    // usuario so recebe as playlist dele
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
    private PlaylistResponseDTO converterParaResponseDTO(Playlist playlist) {

        List<MusicaResumoDTO> musicas = playlist.getMusicas()
                .stream()
                .map(PlaylistMusica::getMusica)
                .map(musica -> new MusicaResumoDTO(
                        musica.getIdMusica(),
                        musica.getTitulo(),
                        musica.getArtistaPrincipal().getNome()
                ))
                .toList();

        return new PlaylistResponseDTO(
                playlist.getId(),
                playlist.getNome(),
                playlist.getDescricao(),
                musicas
        );
    }
    @Transactional
    public PlaylistResponseDTO criarPlaylist(PlaylistRequestDTO dto) {

        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado.")); 
            

        Playlist playlist = new Playlist();
        playlist.setNome(dto.getNome());
        playlist.setDescricao(dto.getDescricao());
        playlist.setUsuario(usuario);

        Playlist playlistSalva = playlistRepository.save(playlist);

        PlaylistResponseDTO responseDTO = new PlaylistResponseDTO();
        responseDTO.setId(playlistSalva.getId());
        responseDTO.setNome(playlistSalva.getNome());
        responseDTO.setDescricao(playlistSalva.getDescricao());
        
        return responseDTO;
    }

}