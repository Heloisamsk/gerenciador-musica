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

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UsuarioRepository usuarioRepository;

    
    public PlaylistService(PlaylistRepository playlistRepository, UsuarioRepository usuarioRepository) {
        this.playlistRepository = playlistRepository;
        this.usuarioRepository = usuarioRepository;
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