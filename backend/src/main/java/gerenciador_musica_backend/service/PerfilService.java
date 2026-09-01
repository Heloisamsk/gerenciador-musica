package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AtualizarPerfilRequestDTO;
import gerenciador_musica_backend.dto.PerfilItemResponseDTO;
import gerenciador_musica_backend.dto.PerfilResponseDTO;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosPerfilInvalidosException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.exception.UsuarioNaoEncontradoException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Perfil;
import gerenciador_musica_backend.model.TipoDestaquePerfil;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.PerfilRepository;
import gerenciador_musica_backend.repository.ReviewRepository;
import gerenciador_musica_backend.repository.SeguidorUsuarioRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArtistaRepository artistaRepository;
    private final MusicaRepository musicaRepository;
    private final AlbumRepository albumRepository;
    private final ReviewRepository reviewRepository;
    private final SeguidorUsuarioRepository seguidorUsuarioRepository;

    public PerfilService(
            PerfilRepository perfilRepository,
            UsuarioRepository usuarioRepository,
            ArtistaRepository artistaRepository,
            MusicaRepository musicaRepository,
            AlbumRepository albumRepository,
            ReviewRepository reviewRepository,
            SeguidorUsuarioRepository seguidorUsuarioRepository
    ) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.artistaRepository = artistaRepository;
        this.musicaRepository = musicaRepository;
        this.albumRepository = albumRepository;
        this.reviewRepository = reviewRepository;
        this.seguidorUsuarioRepository = seguidorUsuarioRepository;
    }

    @Transactional
    public PerfilResponseDTO obterPerfil(Usuario usuarioAutenticado) {
        Usuario usuario = buscarUsuario(usuarioAutenticado);
        Perfil perfil = buscarOuCriarPerfil(usuario);
        return converterResposta(perfil, usuarioAutenticado);
    }

    /*
     * Perfil público de qualquer usuário (ex.: ao clicar no nome do
     * autor de uma review). Reaproveita o mesmo DTO de resposta do
     * perfil autenticado, que já não expõe dados sensíveis como
     * e-mail ou senha. Também informa se o usuário autenticado já
     * segue esse perfil, para o botão de seguir no frontend.
     */
    @Transactional
    public PerfilResponseDTO obterPerfilPublico(
            Long idUsuario,
            Usuario usuarioAutenticado
    ) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(idUsuario));

        Perfil perfil = buscarOuCriarPerfil(usuario);
        return converterResposta(perfil, usuarioAutenticado);
    }

    @Transactional
    public PerfilResponseDTO atualizarPerfil(
            Usuario usuarioAutenticado,
            AtualizarPerfilRequestDTO request
    ) {
        Usuario usuario = buscarUsuario(usuarioAutenticado);
        Perfil perfil = buscarOuCriarPerfil(usuario);

        atualizarIdentidade(usuario, request);
        perfil.setFotoUrl(normalizarOpcional(request.fotoUrl()));
        perfil.setBannerUrl(normalizarOpcional(request.bannerUrl()));
        perfil.setBiografia(normalizarOpcional(request.biografia()));
        perfil.setFraseDestaque(normalizarOpcional(request.fraseDestaque()));

        Artista artistaDestaque = buscarArtista(request.idArtistaDestaque());
        Musica musicaDestaque = buscarMusica(request.idMusicaDestaque());
        Album albumDestaque = buscarAlbum(request.idAlbumDestaque());
        TipoDestaquePerfil tipoPrincipal = validarTipoPrincipal(
                request.tipoDestaquePrincipal(),
                artistaDestaque,
                musicaDestaque,
                albumDestaque
        );

        List<Long> idsArtistas = validarIdsFavoritos(
                request.idsArtistasFavoritos(), "artistas"
        );
        List<Long> idsAlbuns = validarIdsFavoritos(
                request.idsAlbunsFavoritos(), "álbuns"
        );
        List<Long> idsMusicas = validarIdsFavoritos(
                request.idsMusicasFavoritas(), "músicas"
        );

        validarDestaqueNaoRepetido(
                tipoPrincipal,
                artistaDestaque,
                musicaDestaque,
                albumDestaque,
                idsArtistas,
                idsAlbuns,
                idsMusicas
        );
        definirDestaquePrincipal(
                perfil,
                tipoPrincipal,
                artistaDestaque,
                musicaDestaque,
                albumDestaque
        );
        perfil.setArtistasFavoritos(buscarArtistas(idsArtistas));
        perfil.setAlbunsFavoritos(buscarAlbuns(idsAlbuns));
        perfil.setMusicasFavoritas(buscarMusicas(idsMusicas));

        perfilRepository.save(perfil);
        return converterResposta(perfil, usuarioAutenticado);
    }

    private Usuario buscarUsuario(Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null || usuarioAutenticado.getId() == null) {
            throw new DadosPerfilInvalidosException(
                    "Não foi possível identificar o usuário autenticado."
            );
        }

        return usuarioRepository.findById(usuarioAutenticado.getId())
                .orElseThrow(() -> new DadosPerfilInvalidosException(
                        "O usuário autenticado não foi encontrado."
                ));
    }

    private Perfil buscarOuCriarPerfil(Usuario usuario) {
        return perfilRepository.findByUsuario_Id(usuario.getId())
                .orElseGet(() -> perfilRepository.save(new Perfil(usuario)));
    }

    private void atualizarIdentidade(
            Usuario usuario,
            AtualizarPerfilRequestDTO request
    ) {
        String username = normalizarOpcional(request.username());

        if (username != null && usuarioRepository
                .existsByUsernameIgnoreCaseAndIdNot(username, usuario.getId())) {
            throw new DadosPerfilInvalidosException(
                    "Esse username já está sendo usado por outro perfil."
            );
        }

        usuario.setNome(request.nome().trim());
        usuario.setUsername(username);
    }

    private Artista buscarArtista(Long idArtista) {
        if (idArtista == null) {
            return null;
        }
        return artistaRepository.findById(idArtista)
                .orElseThrow(() -> new ArtistaNaoEncontradoException(idArtista));
    }

    private Musica buscarMusica(Long idMusica) {
        if (idMusica == null) {
            return null;
        }
        return musicaRepository.findById(idMusica)
                .orElseThrow(() -> new MusicaNaoEncontradaException(idMusica));
    }

    private Album buscarAlbum(Long idAlbum) {
        if (idAlbum == null) {
            return null;
        }
        return albumRepository.findById(idAlbum)
                .orElseThrow(() -> new AlbumNaoEncontradoException(idAlbum));
    }

    private TipoDestaquePerfil validarTipoPrincipal(
            TipoDestaquePerfil tipo,
            Artista artista,
            Musica musica,
            Album album
    ) {
        if (tipo == null) {
            return primeiroTipoDisponivel(artista, musica, album);
        }

        if (!tipoEstaSelecionado(tipo, artista, musica, album)) {
            throw new DadosPerfilInvalidosException(
                    "Escolha um item válido para o destaque principal."
            );
        }

        return tipo;
    }

    private TipoDestaquePerfil primeiroTipoDisponivel(Perfil perfil) {
        return primeiroTipoDisponivel(
                perfil.getArtistaDestaque(),
                perfil.getMusicaDestaque(),
                perfil.getAlbumDestaque()
        );
    }

    private TipoDestaquePerfil primeiroTipoDisponivel(
            Artista artista,
            Musica musica,
            Album album
    ) {
        if (artista != null) {
            return TipoDestaquePerfil.ARTISTA;
        }
        if (musica != null) {
            return TipoDestaquePerfil.MUSICA;
        }
        if (album != null) {
            return TipoDestaquePerfil.ALBUM;
        }
        return null;
    }

    private void definirDestaquePrincipal(
            Perfil perfil,
            TipoDestaquePerfil tipo,
            Artista artista,
            Musica musica,
            Album album
    ) {
        perfil.setTipoDestaquePrincipal(tipo);
        perfil.setArtistaDestaque(
                tipo == TipoDestaquePerfil.ARTISTA ? artista : null
        );
        perfil.setMusicaDestaque(
                tipo == TipoDestaquePerfil.MUSICA ? musica : null
        );
        perfil.setAlbumDestaque(
                tipo == TipoDestaquePerfil.ALBUM ? album : null
        );
    }

    private List<Long> validarIdsFavoritos(
            List<Long> ids,
            String categoria
    ) {
        if (ids == null) {
            return List.of();
        }
        if (ids.size() > 3) {
            throw new DadosPerfilInvalidosException(
                    "Selecione no máximo três " + categoria + " favoritos."
            );
        }
        if (ids.stream().anyMatch(Objects::isNull)
                || new HashSet<>(ids).size() != ids.size()) {
            throw new DadosPerfilInvalidosException(
                    "A seleção de " + categoria + " favoritos é inválida."
            );
        }
        return List.copyOf(ids);
    }

    private void validarDestaqueNaoRepetido(
            TipoDestaquePerfil tipo,
            Artista artista,
            Musica musica,
            Album album,
            List<Long> idsArtistas,
            List<Long> idsAlbuns,
            List<Long> idsMusicas
    ) {
        if (tipo == null) {
            return;
        }

        boolean repetido = switch (tipo) {
            case ARTISTA -> idsArtistas.contains(artista.getIdArtista());
            case MUSICA -> idsMusicas.contains(musica.getIdMusica());
            case ALBUM -> idsAlbuns.contains(album.getIdAlbum());
        };

        if (repetido) {
            throw new DadosPerfilInvalidosException(
                    "O destaque principal não pode ser repetido nos favoritos."
            );
        }
    }

    private List<Artista> buscarArtistas(List<Long> ids) {
        return ids.stream().map(this::buscarArtista).toList();
    }

    private List<Album> buscarAlbuns(List<Long> ids) {
        return ids.stream().map(this::buscarAlbum).toList();
    }

    private List<Musica> buscarMusicas(List<Long> ids) {
        return ids.stream().map(this::buscarMusica).toList();
    }

    private PerfilResponseDTO converterResposta(
            Perfil perfil,
            Usuario usuarioAutenticado
    ) {
        Usuario usuario = perfil.getUsuario();
        TipoDestaquePerfil tipoPrincipal = perfil.getTipoDestaquePrincipal();

        if (!tipoEstaSelecionado(tipoPrincipal, perfil)) {
            tipoPrincipal = primeiroTipoDisponivel(perfil);
        }

        boolean perfilProprio = usuarioAutenticado != null
                && usuarioAutenticado.getId() != null
                && usuarioAutenticado.getId().equals(usuario.getId());

        boolean seguindo = !perfilProprio
                && usuarioAutenticado != null
                && seguidorUsuarioRepository.existsBySeguidor_IdAndSeguido_Id(
                usuarioAutenticado.getId(),
                usuario.getId()
        );

        return new PerfilResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNome(),
                usuario.getDataCadastro(),
                usuario.getRole(),
                perfil.getFotoUrl(),
                perfil.getBannerUrl(),
                perfil.getBiografia(),
                perfil.getFraseDestaque(),
                tipoPrincipal,
                converterArtista(perfil.getArtistaDestaque()),
                converterMusica(perfil.getMusicaDestaque()),
                converterAlbum(perfil.getAlbumDestaque()),
                perfil.getArtistasFavoritos().stream()
                        .map(this::converterArtista)
                        .toList(),
                perfil.getAlbunsFavoritos().stream()
                        .map(this::converterAlbum)
                        .toList(),
                perfil.getMusicasFavoritas().stream()
                        .map(this::converterMusica)
                        .toList(),
                reviewRepository.countByUsuario_IdAndMusicaIsNotNull(usuario.getId()),
                reviewRepository.countByUsuario_IdAndAlbumIsNotNull(usuario.getId()),
                seguidorUsuarioRepository.countBySeguido_Id(usuario.getId()),
                seguidorUsuarioRepository.countBySeguidor_Id(usuario.getId()),
                perfilProprio,
                seguindo
        );
    }

    private PerfilItemResponseDTO converterArtista(Artista artista) {
        if (artista == null) {
            return null;
        }
        return new PerfilItemResponseDTO(
                TipoDestaquePerfil.ARTISTA,
                artista.getIdArtista(),
                artista.getNome(),
                "Artista",
                artista.getFotoPerfilUrl()
        );
    }

    private PerfilItemResponseDTO converterMusica(Musica musica) {
        if (musica == null) {
            return null;
        }

        Artista artista = musica.getArtistaPrincipal();
        Album album = musica.getAlbum();
        String imagem = null;
        if (album != null) {
            imagem = album.getCapaUrl();
        } else if (artista != null) {
            imagem = artista.getFotoPerfilUrl();
        }

        return new PerfilItemResponseDTO(
                TipoDestaquePerfil.MUSICA,
                musica.getIdMusica(),
                musica.getTitulo(),
                artista == null ? "Música" : artista.getNome(),
                imagem
        );
    }

    private PerfilItemResponseDTO converterAlbum(Album album) {
        if (album == null) {
            return null;
        }
        return new PerfilItemResponseDTO(
                TipoDestaquePerfil.ALBUM,
                album.getIdAlbum(),
                album.getTitulo(),
                album.getArtista().getNome(),
                album.getCapaUrl()
        );
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private boolean tipoEstaSelecionado(
            TipoDestaquePerfil tipo,
            Perfil perfil
    ) {
        return tipoEstaSelecionado(
                tipo,
                perfil.getArtistaDestaque(),
                perfil.getMusicaDestaque(),
                perfil.getAlbumDestaque()
        );
    }

    private boolean tipoEstaSelecionado(
            TipoDestaquePerfil tipo,
            Artista artista,
            Musica musica,
            Album album
    ) {
        if (tipo == null) {
            return false;
        }

        return switch (tipo) {
            case ARTISTA -> artista != null;
            case MUSICA -> musica != null;
            case ALBUM -> album != null;
        };
    }
}