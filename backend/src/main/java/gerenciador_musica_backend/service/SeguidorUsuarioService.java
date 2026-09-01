package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.UsuarioSeguidoResumoDTO;
import gerenciador_musica_backend.exception.SeguirUsuarioInvalidoException;
import gerenciador_musica_backend.exception.UsuarioNaoEncontradoException;
import gerenciador_musica_backend.model.SeguidorUsuario;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.SeguidorUsuarioRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeguidorUsuarioService {

    private final SeguidorUsuarioRepository seguidorUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public SeguidorUsuarioService(
            SeguidorUsuarioRepository seguidorUsuarioRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.seguidorUsuarioRepository = seguidorUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /*
     * Seguir um usuário é idempotente: seguir de novo alguém que já
     * é seguido não faz nada. Um usuário não pode seguir a si mesmo
     * (a constraint ck_usuario_nao_segue_si_mesmo também garante isso
     * no banco, mas validamos antes para devolver um erro 400 claro).
     */
    @Transactional
    public void seguirUsuario(Long idUsuarioAlvo) {
        Usuario usuarioAutenticado = obterUsuarioAutenticado();

        if (idUsuarioAlvo == null) {
            throw new UsuarioNaoEncontradoException(null);
        }

        if (idUsuarioAlvo.equals(usuarioAutenticado.getId())) {
            throw new SeguirUsuarioInvalidoException(
                    "Não é possível seguir a si mesmo."
            );
        }

        if (seguidorUsuarioRepository.existsBySeguidor_IdAndSeguido_Id(
                usuarioAutenticado.getId(),
                idUsuarioAlvo
        )) {
            return;
        }

        Usuario usuarioAlvo = usuarioRepository.findById(idUsuarioAlvo)
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException(idUsuarioAlvo)
                );

        seguidorUsuarioRepository.save(
                new SeguidorUsuario(usuarioAutenticado, usuarioAlvo)
        );
    }

    @Transactional
    public void deixarDeSeguirUsuario(Long idUsuarioAlvo) {
        Usuario usuarioAutenticado = obterUsuarioAutenticado();

        seguidorUsuarioRepository.deleteBySeguidor_IdAndSeguido_Id(
                usuarioAutenticado.getId(),
                idUsuarioAlvo
        );
    }

    @Transactional(readOnly = true)
    public long contarSeguidores(Long idUsuario) {
        return seguidorUsuarioRepository.countBySeguido_Id(idUsuario);
    }

    @Transactional(readOnly = true)
    public long contarSeguindo(Long idUsuario) {
        return seguidorUsuarioRepository.countBySeguidor_Id(idUsuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioSeguidoResumoDTO> listarSeguindo(Long idUsuario) {
        return seguidorUsuarioRepository
                .buscarUsuariosSeguidosPeloUsuario(idUsuario)
                .stream()
                .map(this::converterResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioSeguidoResumoDTO> listarSeguidores(Long idUsuario) {
        return seguidorUsuarioRepository
                .buscarSeguidoresDoUsuario(idUsuario)
                .stream()
                .map(this::converterResumo)
                .toList();
    }

    private UsuarioSeguidoResumoDTO converterResumo(Usuario usuario) {
        return new UsuarioSeguidoResumoDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername()
        );
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