package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AtualizarPerfilRequestDTO;
import gerenciador_musica_backend.dto.PerfilItemResponseDTO;
import gerenciador_musica_backend.dto.PerfilResponseDTO;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosPerfilInvalidosException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
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
import gerenciador_musica_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArtistaRepository artistaRepository;
    private final MusicaRepository musicaRepository;
    private final AlbumRepository albumRepository;

    public PerfilService(
            PerfilRepository perfilRepository,
            UsuarioRepository usuarioRepository,
            ArtistaRepository artistaRepository,
            MusicaRepository musicaRepository,
            AlbumRepository albumRepository
    ) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.artistaRepository = artistaRepository;
        this.musicaRepository = musicaRepository;
        this.albumRepository = albumRepository;
    }

    @Transactional
    public PerfilResponseDTO obterPerfil(Usuario usuarioAutenticado) {
        Usuario usuario = buscarUsuario(usuarioAutenticado);
        Perfil perfil = buscarOuCriarPerfil(usuario);
        return converterResposta(perfil);
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

        perfil.setArtistaDestaque(buscarArtista(request.idArtistaDestaque()));
        perfil.setMusicaDestaque(buscarMusica(request.idMusicaDestaque()));
        perfil.setAlbumDestaque(buscarAlbum(request.idAlbumDestaque()));
        perfil.setTipoDestaquePrincipal(validarTipoPrincipal(
                request.tipoDestaquePrincipal(),
                perfil
        ));

        perfilRepository.save(perfil);
        return converterResposta(perfil);
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
            Perfil perfil
    ) {
        if (tipo == null) {
            return primeiroTipoDisponivel(perfil);
        }

        if (!tipoEstaSelecionado(tipo, perfil)) {
            throw new DadosPerfilInvalidosException(
                    "O destaque principal deve ser um favorito selecionado."
            );
        }

        return tipo;
    }

    private TipoDestaquePerfil primeiroTipoDisponivel(Perfil perfil) {
        if (perfil.getArtistaDestaque() != null) {
            return TipoDestaquePerfil.ARTISTA;
        }
        if (perfil.getMusicaDestaque() != null) {
            return TipoDestaquePerfil.MUSICA;
        }
        if (perfil.getAlbumDestaque() != null) {
            return TipoDestaquePerfil.ALBUM;
        }
        return null;
    }

    private PerfilResponseDTO converterResposta(Perfil perfil) {
        Usuario usuario = perfil.getUsuario();
        TipoDestaquePerfil tipoPrincipal = perfil.getTipoDestaquePrincipal();

        if (!tipoEstaSelecionado(tipoPrincipal, perfil)) {
            tipoPrincipal = primeiroTipoDisponivel(perfil);
        }

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
                converterAlbum(perfil.getAlbumDestaque())
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
        String imagem = album != null
                ? album.getCapaUrl()
                : artista == null ? null : artista.getFotoPerfilUrl();

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
        if (tipo == null) {
            return false;
        }

        return switch (tipo) {
            case ARTISTA -> perfil.getArtistaDestaque() != null;
            case MUSICA -> perfil.getMusicaDestaque() != null;
            case ALBUM -> perfil.getAlbumDestaque() != null;
        };
    }
}
