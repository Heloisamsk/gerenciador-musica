package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.PaginaResponseDTO;
import gerenciador_musica_backend.dto.ReviewAtualizacaoRequestDTO;
import gerenciador_musica_backend.dto.ReviewRequestDTO;
import gerenciador_musica_backend.dto.ReviewResponseDTO;
import gerenciador_musica_backend.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Reviews de músicas e álbuns (nota de 1 a 5 estrelas, com texto
 * opcional). Cada usuário pode avaliar um mesmo alvo uma única vez;
 * para mudar de ideia, ele edita ou exclui a review existente.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> criar(
            @RequestBody ReviewRequestDTO request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.criarReview(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> atualizar(
            @PathVariable("id") Long id,
            @RequestBody ReviewAtualizacaoRequestDTO request
    ) {
        return ResponseEntity.ok(
                reviewService.atualizarReview(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        reviewService.excluirReview(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/reviews/{id} — página individual da review: qualquer
     * usuário autenticado pode ver; só o autor edita ou exclui.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> buscarPorId(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(reviewService.buscarPorId(id));
    }

    /**
     * GET /api/reviews — feed com as reviews mais recentes de todos os
     * usuários, inspirado na atividade de amigos do Letterboxd.
     */
    @GetMapping
    public ResponseEntity<PaginaResponseDTO<ReviewResponseDTO>> listarFeed(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(
                reviewService.listarFeed(page, size)
        );
    }

    @GetMapping("/minhas")
    public ResponseEntity<PaginaResponseDTO<ReviewResponseDTO>> listarMinhas(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(
                reviewService.listarMinhas(page, size)
        );
    }

    /**
     * GET /api/reviews/seguindo — feed apenas com as reviews de
     * usuários que o autenticado segue.
     */
    @GetMapping("/seguindo")
    public ResponseEntity<PaginaResponseDTO<ReviewResponseDTO>> listarSeguindo(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(
                reviewService.listarSeguindo(page, size)
        );
    }

    @GetMapping("/musicas/{idMusica}")
    public ResponseEntity<PaginaResponseDTO<ReviewResponseDTO>> listarPorMusica(
            @PathVariable Long idMusica,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(
                reviewService.listarPorMusica(idMusica, page, size)
        );
    }

    @GetMapping("/albuns/{idAlbum}")
    public ResponseEntity<PaginaResponseDTO<ReviewResponseDTO>> listarPorAlbum(
            @PathVariable Long idAlbum,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(
                reviewService.listarPorAlbum(idAlbum, page, size)
        );
    }
}