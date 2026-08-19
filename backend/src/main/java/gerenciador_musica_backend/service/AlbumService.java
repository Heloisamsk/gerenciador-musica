package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.exception.AlbumDuplicadoException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.repository.AlbumRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaService artistaService;

    public AlbumService(
            AlbumRepository albumRepository,
            ArtistaService artistaService
    ) {
        this.albumRepository = albumRepository;
        this.artistaService = artistaService;
    }

    @Transactional
    public AlbumResponseDTO cadastrarAlbum(AlbumRequestDTO request) {

        String titulo = request.titulo().strip();

        Artista artista = artistaService.buscarEntidadePorId(
                request.idArtista()
        );

        boolean albumJaExiste =
                albumRepository
                        .existsByTituloIgnoreCaseAndArtistaIdArtistaAndAnoLancamento(
                                titulo,
                                artista.getIdArtista(),
                                request.anoLancamento()
                        );

        if (albumJaExiste) {
            throw new AlbumDuplicadoException(
                    "Já existe um álbum com o título '"
                            + titulo
                            + "' para esse artista no ano de "
                            + request.anoLancamento()
            );
        }

        Album album = new Album(
                artista,
                titulo,
                request.anoLancamento(),
                request.capaUrl()
        );

        Album albumSalvo = albumRepository.save(album);

        return converterParaResponse(albumSalvo);
    }

    private AlbumResponseDTO converterParaResponse(Album album) {

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
                artistaResumo
        );
    }
}