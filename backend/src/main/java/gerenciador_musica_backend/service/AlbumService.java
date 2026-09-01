package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.AlbumCapaPublicaDTO;
import gerenciador_musica_backend.dto.AlbumDetalheDTO;
import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.dto.MusicaAlbumDTO;
import gerenciador_musica_backend.exception.AlbumDuplicadoException;
import gerenciador_musica_backend.exception.AlbumEmUsoException;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosAlbumInvalidosException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.CurtidaAlbumRepository;
import gerenciador_musica_backend.repository.CurtidaMusicaRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.projection.MusicaCatalogoProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AlbumService {

    private static final short ANO_MINIMO = 1800;
    private static final short ANO_MAXIMO = 2100;

    private final AlbumRepository albumRepository;
    private final ArtistaService artistaService;
    private final MusicaRepository musicaRepository;
    private final CurtidaAlbumRepository curtidaAlbumRepository;
    private final CurtidaMusicaRepository curtidaMusicaRepository;

    public AlbumService(
            AlbumRepository albumRepository,
            ArtistaService artistaService,
            MusicaRepository musicaRepository,
            CurtidaAlbumRepository curtidaAlbumRepository,
            CurtidaMusicaRepository curtidaMusicaRepository
    ) {
        this.albumRepository = albumRepository;
        this.artistaService = artistaService;
        this.musicaRepository = musicaRepository;
        this.curtidaAlbumRepository = curtidaAlbumRepository;
        this.curtidaMusicaRepository = curtidaMusicaRepository;
    }

    @Transactional
    public AlbumResponseDTO cadastrarAlbum(
            AlbumRequestDTO request
    ) {
        validarRequest(request);

        String tituloNormalizado = normalizarCampoObrigatorio(
                request.titulo(),
                "O título do álbum"
        );

        String capaUrlNormalizada = normalizarCampoOpcional(
                request.capaUrl()
        );

        Artista artista = artistaService.buscarEntidadePorId(
                request.idArtista()
        );

        verificarDuplicidade(
                tituloNormalizado,
                artista.getIdArtista(),
                request.anoLancamento()
        );

        Album album = new Album(
                artista,
                tituloNormalizado,
                request.anoLancamento(),
                capaUrlNormalizada
        );

        Album albumSalvo = albumRepository.save(album);

        return converterParaResponse(albumSalvo, false);
    }

    @Transactional
    public AlbumResponseDTO atualizarAlbum(
            Long idAlbum,
            AlbumAtualizacaoRequestDTO request
    ) {
        validarRequestAtualizacao(request);

        Album album = obterEntidadePorId(idAlbum);

        String tituloNormalizado = normalizarCampoObrigatorio(
                request.titulo(),
                "O título do álbum"
        );
        String capaUrlNormalizada = normalizarCampoOpcional(
                request.capaUrl()
        );

        verificarDuplicidade(
                tituloNormalizado,
                album.getArtista().getIdArtista(),
                request.anoLancamento(),
                idAlbum
        );

        album.setTitulo(tituloNormalizado);
        album.setAnoLancamento(request.anoLancamento());
        album.setCapaUrl(capaUrlNormalizada);

        return converterParaResponse(album, false);
    }

    @Transactional(readOnly = true)
    public List<AlbumResponseDTO> listarAlbuns() {
        List<Album> albuns = albumRepository
                .findAll(
                        Sort.by(
                                Sort.Order.asc("titulo"),
                                Sort.Order.asc("anoLancamento")
                        )
                );

        return converterParaResponseComCurtidas(albuns);
    }

    @Transactional(readOnly = true)
    public List<AlbumResponseDTO> listarAlbunsCurtidos() {
        Long usuarioId = obterUsuarioAutenticado().getId();

        return curtidaAlbumRepository
                .buscarAlbunsCurtidosPeloUsuario(usuarioId)
                .stream()
                .map(album -> converterParaResponse(album, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlbumResponseDTO> listarAlbunsPorArtista(
            Long idArtista
    ) {
        validarIdPositivo(
                idArtista,
                "O ID do artista deve ser válido."
        );

        artistaService.buscarEntidadePorId(idArtista);

        List<Album> albuns = albumRepository
                .findByArtistaIdArtistaOrderByTituloAscAnoLancamentoAsc(
                        idArtista
                );

        return converterParaResponseComCurtidas(albuns);
    }

    @Transactional(readOnly = true)
    public List<AlbumResponseDTO> buscarPorTitulo(String termo, int limite) {
        if (termo == null || termo.isBlank()) {
            return List.of();
        }

        List<Album> albuns = albumRepository
                .findByTituloContainingIgnoreCaseOrderByTitulo(
                        termo.strip(),
                        PageRequest.of(0, limite)
                );

        return converterParaResponseComCurtidas(albuns);
    }

    private List<AlbumResponseDTO> converterParaResponseComCurtidas(
            List<Album> albuns
    ) {
        List<Long> ids = albuns.stream()
                .map(Album::getIdAlbum)
                .toList();

        Set<Long> idsCurtidos = ids.isEmpty()
                ? Set.of()
                : curtidaAlbumRepository.buscarIdsCurtidosPeloUsuario(
                        obterUsuarioAutenticado().getId(),
                        ids
                );

        return albuns.stream()
                .map(album -> converterParaResponse(
                        album,
                        idsCurtidos.contains(album.getIdAlbum())
                ))
                .toList();
    }

    /*
     * Usado pela tela pública de login/cadastro para exibir um
     * fundo decorativo com capas reais do catálogo, sem exigir
     * autenticação. Retorna apenas dados não sensíveis.
     */
    @Transactional(readOnly = true)
    public List<AlbumCapaPublicaDTO> listarCapasPublicas() {
        return albumRepository
                .findByCapaUrlIsNotNull()
                .stream()
                .map(album -> new AlbumCapaPublicaDTO(
                        album.getIdAlbum(),
                        album.getTitulo(),
                        album.getCapaUrl()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public AlbumResponseDTO buscarPorId(Long idAlbum) {
        Album album = obterEntidadePorId(idAlbum);

        return converterParaResponse(album);
    }

    @Transactional(readOnly = true)
    public AlbumDetalheDTO buscarDetalhesCatalogo(Long idAlbum) {
        validarIdPositivo(
                idAlbum,
                "O ID do álbum deve ser válido."
        );

        var album = albumRepository
                .buscarCatalogoPorId(idAlbum)
                .orElseThrow(
                        () -> new AlbumNaoEncontradoException(idAlbum)
                );

        Long usuarioId = obterUsuarioAutenticado().getId();

        List<MusicaCatalogoProjection> musicasProjecao = musicaRepository
                .buscarCatalogoPorAlbum(idAlbum);

        List<Long> idsMusicas = musicasProjecao.stream()
                .map(MusicaCatalogoProjection::getIdMusica)
                .toList();

        Set<Long> idsMusicasCurtidas = idsMusicas.isEmpty()
                ? Set.of()
                : curtidaMusicaRepository.buscarIdsCurtidosPeloUsuario(
                        usuarioId,
                        idsMusicas
                );

        List<MusicaAlbumDTO> musicas = musicasProjecao.stream()
                .map(musica -> converterMusicaAlbum(
                        musica,
                        idsMusicasCurtidas.contains(musica.getIdMusica())
                ))
                .toList();

        boolean curtidaAlbum = curtidaAlbumRepository
                .existsByUsuario_IdAndAlbum_IdAlbum(usuarioId, idAlbum);

        return new AlbumDetalheDTO(
                CatalogoProjectionMapper.converterAlbum(album, curtidaAlbum),
                reunirGeneros(musicas),
                musicas
        );
    }

    @Transactional(readOnly = true)
    public Album buscarEntidadePorId(Long idAlbum) {
        return obterEntidadePorId(idAlbum);
    }

    @Transactional
    public void excluirAlbum(Long idAlbum) {
        Album album = obterEntidadePorId(idAlbum);

        if (musicaRepository.existsByAlbum_IdAlbum(idAlbum)) {
            throw new AlbumEmUsoException(
                    "Não é possível excluir o álbum porque "
                            + "ele possui músicas associadas."
            );
        }

        /*
         * A FK perfil.id_album_destaque usa ON DELETE SET NULL.
         * O artista não recebe cascata e permanece cadastrado.
         */
        albumRepository.delete(album);
    }

    @Transactional(readOnly = true)
    public Album buscarAlbumDoArtista(
            Long idAlbum,
            Artista artistaPrincipal
    ) {
        // A música pode representar um single e não possuir álbum.
        if (idAlbum == null) {
            return null;
        }

        if (artistaPrincipal == null
                || artistaPrincipal.getIdArtista() == null) {
            throw new DadosAlbumInvalidosException(
                    "O artista principal da música é obrigatório."
            );
        }

        Album album = obterEntidadePorId(idAlbum);

        boolean pertenceAoArtista = Objects.equals(
                album.getArtista().getIdArtista(),
                artistaPrincipal.getIdArtista()
        );

        if (!pertenceAoArtista) {
            throw new DadosAlbumInvalidosException(
                    "O álbum selecionado não pertence ao "
                            + "artista principal da música."
            );
        }

        return album;
    }

    /*
     * Método interno sem @Transactional.
     *
     * Os métodos públicos acima possuem a configuração transacional
     * necessária e utilizam este método para compartilhar a busca.
     * Isso evita chamadas internas entre métodos @Transactional.
     */
    private Album obterEntidadePorId(Long idAlbum) {
        validarIdPositivo(
                idAlbum,
                "O ID do álbum deve ser válido."
        );

        return albumRepository
                .findById(idAlbum)
                .orElseThrow(
                        () -> new AlbumNaoEncontradoException(
                                idAlbum
                        )
                );
    }

    private void validarRequest(AlbumRequestDTO request) {
        if (request == null) {
            throw new DadosAlbumInvalidosException(
                    "Os dados do álbum são obrigatórios."
            );
        }

        validarIdPositivo(
                request.idArtista(),
                "O ID do artista deve ser válido."
        );

        validarAnoLancamento(request.anoLancamento());
    }

    private void validarRequestAtualizacao(
            AlbumAtualizacaoRequestDTO request
    ) {
        if (request == null) {
            throw new DadosAlbumInvalidosException(
                    "Os dados do álbum são obrigatórios."
            );
        }

        validarAnoLancamento(request.anoLancamento());
    }

    private void validarAnoLancamento(Short anoLancamento) {
        if (anoLancamento == null
                || anoLancamento < ANO_MINIMO
                || anoLancamento > ANO_MAXIMO) {
            throw new DadosAlbumInvalidosException(
                    "O ano do álbum deve estar entre 1800 e 2100."
            );
        }
    }

    private void verificarDuplicidade(
            String titulo,
            Long idArtista,
            Short anoLancamento
    ) {
        boolean albumJaExiste = albumRepository
                .existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamento(
                        titulo,
                        idArtista,
                        anoLancamento
                );

        validarDuplicidade(albumJaExiste, titulo, anoLancamento);
    }

    private void verificarDuplicidade(
            String titulo,
            Long idArtista,
            Short anoLancamento,
            Long idAlbum
    ) {
        boolean albumJaExiste = albumRepository
                .existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamentoAndIdAlbumNot(
                        titulo,
                        idArtista,
                        anoLancamento,
                        idAlbum
                );

        validarDuplicidade(albumJaExiste, titulo, anoLancamento);
    }

    private void validarDuplicidade(
            boolean albumJaExiste,
            String titulo,
            Short anoLancamento
    ) {
        if (!albumJaExiste) {
            return;
        }

        throw new AlbumDuplicadoException(
                "Já existe um álbum com o título '"
                        + titulo
                        + "' para esse artista no ano de "
                        + anoLancamento
                        + "."
        );
    }

    private void validarIdPositivo(
            Long id,
            String mensagem
    ) {
        if (id == null || id <= 0) {
            throw new DadosAlbumInvalidosException(mensagem);
        }
    }

    private String normalizarCampoObrigatorio(
            String valor,
            String nomeCampo
    ) {
        if (valor == null) {
            throw new DadosAlbumInvalidosException(
                    nomeCampo + " é obrigatório."
            );
        }

        String valorNormalizado = valor
                .strip()
                .replaceAll("\\s+", " ");

        if (valorNormalizado.isBlank()) {
            throw new DadosAlbumInvalidosException(
                    nomeCampo + " não pode ficar vazio."
            );
        }

        return valorNormalizado;
    }

    private String normalizarCampoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.strip();
    }

    private AlbumResponseDTO converterParaResponse(
            Album album
    ) {
        boolean curtida = curtidaAlbumRepository
                .existsByUsuario_IdAndAlbum_IdAlbum(
                        obterUsuarioAutenticado().getId(),
                        album.getIdAlbum()
                );

        return converterParaResponse(album, curtida);
    }

    private AlbumResponseDTO converterParaResponse(
            Album album,
            boolean curtida
    ) {
        Artista artista = album.getArtista();

        ArtistaResumoDTO artistaResumo = new ArtistaResumoDTO(
                artista.getIdArtista(),
                artista.getNome(),
                artista.getNomeCompleto(),
                artista.getDescricao(),
                artista.getFotoPerfilUrl()
        );

        return new AlbumResponseDTO(
                album.getIdAlbum(),
                album.getTitulo(),
                album.getAnoLancamento(),
                album.getCapaUrl(),
                artistaResumo,
                curtida
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

    private MusicaAlbumDTO converterMusicaAlbum(
            MusicaCatalogoProjection musica,
            boolean curtida
    ) {
        return new MusicaAlbumDTO(
                musica.getIdMusica(),
                musica.getTitulo(),
                musica.getDuracaoSegundos(),
                CatalogoProjectionMapper.separarGeneros(
                        musica.getGeneros()
                ),
                curtida
        );
    }

    private List<String> reunirGeneros(List<MusicaAlbumDTO> musicas) {
        return musicas.stream()
                .flatMap(musica -> musica.generos().stream())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

}
