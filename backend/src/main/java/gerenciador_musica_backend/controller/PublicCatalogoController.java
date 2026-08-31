package gerenciador_musica_backend.controller;

import gerenciador_musica_backend.dto.AlbumCapaPublicaDTO;
import gerenciador_musica_backend.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * Endpoints públicos (sem autenticação) usados para compor telas
 * como login e cadastro, onde ainda não existe um usuário logado.
 */
@RestController
@RequestMapping("/api/public/albuns")
public class PublicCatalogoController {

    private final AlbumService albumService;

    public PublicCatalogoController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping("/capas")
    public ResponseEntity<List<AlbumCapaPublicaDTO>> listarCapas() {
        return ResponseEntity.ok(albumService.listarCapasPublicas());
    }
}
