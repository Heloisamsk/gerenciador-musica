package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.PaginaResponseDTO;
import gerenciador_musica_backend.dto.ReviewAlvoDTO;
import gerenciador_musica_backend.dto.ReviewAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.ReviewAutorDTO;
import gerenciador_musica_backend.dto.ReviewRequestDTO;
import gerenciador_musica_backend.dto.ReviewResponseDTO;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosReviewInvalidosException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.exception.ReviewAcessoNegadoException;
import gerenciador_musica_backend.exception.ReviewJaExisteException;
import gerenciador_musica_backend.exception.ReviewNaoEncontradaException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.model.Review;
import gerenciador_musica_backend.model.TipoAlvoReview;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.ReviewRepository;
import gerenciador_musica_backend.repository.SeguidorUsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Service
public class ReviewService {

    private static final int TAMANHO_PAGINA_PADRAO = 20;
    private static final int TAMANHO_PAGINA_MAXIMO = 100;

    private static final short NOTA_MINIMA = 1;
    private static final short NOTA_MAXIMA = 5;

    private final ReviewRepository reviewRepository;
    private final MusicaRepository musicaRepository;
    private final AlbumRepository albumRepository;
    private final SeguidorUsuarioRepository seguidorUsuarioRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            MusicaRepository musicaRepository,
            AlbumRepository albumRepository,
            SeguidorUsuarioRepository seguidorUsuarioRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.musicaRepository = musicaRepository;
        this.albumRepository = albumRepository;
        this.seguidorUsuarioRepository = seguidorUsuarioRepository;
    }

    @Transactional
    public ReviewResponseDTO criarReview(ReviewRequestDTO request) {
        validarRequest(request);

        Usuario usuario = obterUsuarioAutenticado();
        String textoNormalizado = normalizarTexto(request.texto());

        Review review = request.idMusica() != null
                ? criarReviewDeMusica(
                usuario,
                request.idMusica(),
                request.nota(),
                textoNormalizado
        )
                : criarReviewDeAlbum(
                usuario,
                request.idAlbum(),
                request.nota(),
                textoNormalizado
        );

        Review reviewSalva = reviewRepository.save(review);

        return converterParaResponse(reviewSalva, usuario);
    }

    @Transactional
    public ReviewResponseDTO atualizarReview(
            Long idReview,
            ReviewAtualizacaoRequestDTO request
    ) {
        validarNota(request == null ? null : request.nota());

        Usuario usuario = obterUsuarioAutenticado();
        Review review = obterEntidadePorId(idReview);

        verificarProprietario(review, usuario);

        review.setNota(request.nota());
        review.setTexto(normalizarTexto(request.texto()));

        return converterParaResponse(review, usuario);
    }

    /*
     * Qualquer usuário autenticado pode ver qualquer review (a página
     * da review é pública dentro do app, só a edição/exclusão é
     * restrita ao autor). minhaReview no retorno diz ao frontend se
     * deve mostrar os controles de editar/excluir.
     */
    @Transactional(readOnly = true)
    public ReviewResponseDTO buscarPorId(Long idReview) {
        Usuario usuario = obterUsuarioAutenticado();
        Review review = obterEntidadePorId(idReview);

        return converterParaResponse(review, usuario);
    }

    @Transactional
    public void excluirReview(Long idReview) {
        Usuario usuario = obterUsuarioAutenticado();
        Review review = obterEntidadePorId(idReview);

        verificarProprietario(review, usuario);

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<ReviewResponseDTO> listarFeed(
            Integer pagina,
            Integer tamanhoPagina
    ) {
        return montarPagina(
                pagina,
                tamanhoPagina,
                reviewRepository::findAllByOrderByCriadaEmDesc
        );
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<ReviewResponseDTO> listarMinhas(
            Integer pagina,
            Integer tamanhoPagina
    ) {
        Usuario usuario = obterUsuarioAutenticado();

        return montarPagina(
                pagina,
                tamanhoPagina,
                paginacao -> reviewRepository.findByUsuario_IdOrderByCriadaEmDesc(
                        usuario.getId(),
                        paginacao
                )
        );
    }

    /*
     * GET /api/reviews/seguindo — feed das reviews mais recentes
     * apenas dos usuários que o autenticado segue. Se ele não segue
     * ninguém ainda, devolve uma página vazia em vez de disparar uma
     * consulta "IN ()" (inválida em SQL).
     */
    @Transactional(readOnly = true)
    public PaginaResponseDTO<ReviewResponseDTO> listarSeguindo(
            Integer pagina,
            Integer tamanhoPagina
    ) {
        Usuario usuario = obterUsuarioAutenticado();

        List<Long> idsSeguidos = seguidorUsuarioRepository
                .buscarIdsSeguidosPeloUsuario(usuario.getId());

        if (idsSeguidos.isEmpty()) {
            return new PaginaResponseDTO<>(
                    List.of(),
                    validarPagina(pagina),
                    validarTamanhoPagina(tamanhoPagina),
                    0,
                    0
            );
        }

        return montarPagina(
                pagina,
                tamanhoPagina,
                paginacao -> reviewRepository.findByUsuario_IdInOrderByCriadaEmDesc(
                        idsSeguidos,
                        paginacao
                )
        );
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<ReviewResponseDTO> listarPorMusica(
            Long idMusica,
            Integer pagina,
            Integer tamanhoPagina
    ) {
        return montarPagina(
                pagina,
                tamanhoPagina,
                paginacao -> reviewRepository.findByMusica_IdMusicaOrderByCriadaEmDesc(
                        idMusica,
                        paginacao
                )
        );
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<ReviewResponseDTO> listarPorAlbum(
            Long idAlbum,
            Integer pagina,
            Integer tamanhoPagina
    ) {
        return montarPagina(
                pagina,
                tamanhoPagina,
                paginacao -> reviewRepository.findByAlbum_IdAlbumOrderByCriadaEmDesc(
                        idAlbum,
                        paginacao
                )
        );
    }

    private PaginaResponseDTO<ReviewResponseDTO> montarPagina(
            Integer pagina,
            Integer tamanhoPagina,
            Function<Pageable, Page<Review>> consulta
    ) {
        Usuario usuario = obterUsuarioAutenticado();

        Pageable pageable = PageRequest.of(
                validarPagina(pagina),
                validarTamanhoPagina(tamanhoPagina),
                Sort.by(Sort.Direction.DESC, "criadaEm")
        );

        Page<Review> resultado = consulta.apply(pageable);

        return new PaginaResponseDTO<>(
                resultado.getContent().stream()
                        .map(review -> converterParaResponse(review, usuario))
                        .toList(),
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages()
        );
    }

    private int validarPagina(Integer pagina) {
        if (pagina == null) {
            return 0;
        }

        if (pagina < 0) {
            throw new DadosReviewInvalidosException(
                    "O número da página não pode ser negativo."
            );
        }

        return pagina;
    }

    private int validarTamanhoPagina(Integer tamanhoPagina) {
        if (tamanhoPagina == null) {
            return TAMANHO_PAGINA_PADRAO;
        }

        if (tamanhoPagina <= 0) {
            throw new DadosReviewInvalidosException(
                    "O tamanho da página deve ser maior que zero."
            );
        }

        return Math.min(tamanhoPagina, TAMANHO_PAGINA_MAXIMO);
    }

    private Review criarReviewDeMusica(
            Usuario usuario,
            Long idMusica,
            Short nota,
            String texto
    ) {
        Musica musica = musicaRepository.findById(idMusica)
                .orElseThrow(() -> new MusicaNaoEncontradaException(idMusica));

        if (reviewRepository.existsByUsuario_IdAndMusica_IdMusica(
                usuario.getId(),
                idMusica
        )) {
            throw new ReviewJaExisteException(
                    "Você já avaliou esta música."
            );
        }

        return new Review(usuario, musica, null, nota, texto);
    }

    private Review criarReviewDeAlbum(
            Usuario usuario,
            Long idAlbum,
            Short nota,
            String texto
    ) {
        Album album = albumRepository.findById(idAlbum)
                .orElseThrow(() -> new AlbumNaoEncontradoException(idAlbum));

        if (reviewRepository.existsByUsuario_IdAndAlbum_IdAlbum(
                usuario.getId(),
                idAlbum
        )) {
            throw new ReviewJaExisteException(
                    "Você já avaliou este álbum."
            );
        }

        return new Review(usuario, null, album, nota, texto);
    }

    private Review obterEntidadePorId(Long idReview) {
        if (idReview == null || idReview <= 0) {
            throw new ReviewNaoEncontradaException(idReview);
        }

        return reviewRepository.findById(idReview)
                .orElseThrow(() -> new ReviewNaoEncontradaException(idReview));
    }

    private void verificarProprietario(Review review, Usuario usuario) {
        if (!review.getUsuario().getId().equals(usuario.getId())) {
            throw new ReviewAcessoNegadoException(
                    "Você não possui permissão para alterar esta review."
            );
        }
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

    private void validarRequest(ReviewRequestDTO request) {
        if (request == null) {
            throw new DadosReviewInvalidosException(
                    "Os dados da review são obrigatórios."
            );
        }

        boolean temMusica = request.idMusica() != null;
        boolean temAlbum = request.idAlbum() != null;

        if (temMusica == temAlbum) {
            throw new DadosReviewInvalidosException(
                    "Informe exatamente um alvo para a review: "
                            + "uma música ou um álbum."
            );
        }

        validarNota(request.nota());
    }

    private void validarNota(Short nota) {
        if (nota == null || nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new DadosReviewInvalidosException(
                    "A nota deve estar entre "
                            + NOTA_MINIMA + " e " + NOTA_MAXIMA + "."
            );
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.strip();
    }

    private ReviewResponseDTO converterParaResponse(
            Review review,
            Usuario usuarioAutenticado
    ) {
        return new ReviewResponseDTO(
                review.getIdReview(),
                new ReviewAutorDTO(
                        review.getUsuario().getId(),
                        review.getUsuario().getNome()
                ),
                converterAlvo(review),
                review.getNota(),
                review.getTexto(),
                review.getCriadaEm(),
                review.getAtualizadaEm(),
                review.getUsuario().getId().equals(usuarioAutenticado.getId())
        );
    }

    private ReviewAlvoDTO converterAlvo(Review review) {
        if (review.getMusica() != null) {
            Musica musica = review.getMusica();
            Artista artistaPrincipal = musica.getArtistaPrincipal();
            String artista = artistaPrincipal != null
                    ? artistaPrincipal.getNome()
                    : null;
            String capaUrl = musica.getAlbum() != null
                    ? musica.getAlbum().getCapaUrl()
                    : null;

            return new ReviewAlvoDTO(
                    TipoAlvoReview.MUSICA,
                    musica.getIdMusica(),
                    musica.getTitulo(),
                    artista,
                    capaUrl
            );
        }

        Album album = review.getAlbum();

        return new ReviewAlvoDTO(
                TipoAlvoReview.ALBUM,
                album.getIdAlbum(),
                album.getTitulo(),
                album.getArtista().getNome(),
                album.getCapaUrl()
        );
    }
}