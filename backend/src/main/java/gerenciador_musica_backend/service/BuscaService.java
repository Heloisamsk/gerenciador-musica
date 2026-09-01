package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.dto.BuscaResultadoDTO;
import gerenciador_musica_backend.dto.MusicaFiltroDTO;
import gerenciador_musica_backend.dto.MusicaListagemDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BuscaService {

    private static final int LIMITE_POR_TIPO = 5;

    private final MusicaService musicaService;
    private final AlbumService albumService;
    private final ArtistaService artistaService;

    public BuscaService(
            MusicaService musicaService,
            AlbumService albumService,
            ArtistaService artistaService
    ) {
        this.musicaService = musicaService;
        this.albumService = albumService;
        this.artistaService = artistaService;
    }

    @Transactional(readOnly = true)
    public BuscaResultadoDTO buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return new BuscaResultadoDTO(List.of(), List.of(), List.of());
        }

        String termoNormalizado = termo.strip();

        List<MusicaListagemDTO> musicas = musicaService.pesquisarMusicas(
                new MusicaFiltroDTO(termoNormalizado, null, null, null, null),
                0,
                LIMITE_POR_TIPO,
                null
        ).itens();

        List<AlbumResponseDTO> albuns = albumService.buscarPorTitulo(
                termoNormalizado,
                LIMITE_POR_TIPO
        );

        List<ArtistaResponseDTO> artistas = artistaService.buscarPorNome(
                termoNormalizado,
                LIMITE_POR_TIPO
        );

        return new BuscaResultadoDTO(musicas, albuns, artistas);
    }
}
