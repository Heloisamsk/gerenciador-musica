package gerenciador_musica_backend.service;

import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.SeguidorArtista;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.SeguidorArtistaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeguidorArtistaService {

    private final SeguidorArtistaRepository seguidorArtistaRepository;
    private final ArtistaRepository artistaRepository;

    public SeguidorArtistaService(
            SeguidorArtistaRepository seguidorArtistaRepository,
            ArtistaRepository artistaRepository
    ) {
        this.seguidorArtistaRepository = seguidorArtistaRepository;
        this.artistaRepository = artistaRepository;
    }

    /*
     * Seguir um artista é idempotente: seguir de novo um artista já
     * seguido não faz nada.
     */
    @Transactional
    public void seguirArtista(Long artistaId) {
        Usuario usuario = obterUsuarioAutenticado();

        if (seguidorArtistaRepository
                .existsByUsuario_IdAndArtista_IdArtista(
                        usuario.getId(),
                        artistaId
                )) {
            return;
        }

        Artista artista = artistaRepository.findById(artistaId)
                .orElseThrow(() ->
                        new ArtistaNaoEncontradoException(artistaId)
                );

        seguidorArtistaRepository.save(
                new SeguidorArtista(usuario, artista)
        );
    }

    @Transactional
    public void deixarDeSeguirArtista(Long artistaId) {
        Usuario usuario = obterUsuarioAutenticado();

        seguidorArtistaRepository.deleteByUsuario_IdAndArtista_IdArtista(
                usuario.getId(),
                artistaId
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
