package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.repository.AlbumRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;

    public AlbumService(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    @Transactional(readOnly = true)
    public List<AlbumResponseDTO> listarAlbuns() {
        return albumRepository
                .findAll(Sort.by(Sort.Direction.ASC, "titulo"))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    private AlbumResponseDTO converterParaResponse(Album album) {
        return new AlbumResponseDTO(
                album.getIdAlbum(),
                album.getTitulo(),
                album.getAnoLancamento(),
                album.getCapaUrl(),
                converterArtistaParaResumo(album.getArtista())
        );
    }

    private ArtistaResumoDTO converterArtistaParaResumo(Artista artista) {
        return new ArtistaResumoDTO(
                artista.getIdArtista(),
                artista.getNome(),
                artista.getNomeCompleto(),
                artista.getDescricao(),
                artista.getFotoPerfilUrl()
        );
    }
}
