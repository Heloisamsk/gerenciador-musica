package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumRequestDTO;
import gerenciador_musica_backend.dto.AlbumResponseDTO;
import gerenciador_musica_backend.dto.ArtistaResumoDTO;
import gerenciador_musica_backend.exception.AlbumDuplicadoException;
import gerenciador_musica_backend.exception.AlbumNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosAlbumInvalidosException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.repository.AlbumRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class AlbumService {

    private static final short ANO_MINIMO = 1800;
    private static final short ANO_MAXIMO = 2100;

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

        return converterParaResponse(albumRepository.save(album));
    }

    @Transactional(readOnly = true)
    public List<AlbumResponseDTO> listarAlbuns() {
        return albumRepository
                .findAll(Sort.by(
                        Sort.Order.asc("titulo"),
                        Sort.Order.asc("anoLancamento")
                ))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlbumResponseDTO> listarAlbunsPorArtista(Long idArtista) {
        validarIdPositivo(
                idArtista,
                "O ID do artista deve ser válido."
        );

        artistaService.buscarEntidadePorId(idArtista);

        return albumRepository
                .findByArtistaIdArtistaOrderByTituloAscAnoLancamentoAsc(
                        idArtista
                )
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlbumResponseDTO buscarPorId(Long idAlbum) {
        return converterParaResponse(buscarEntidadePorId(idAlbum));
    }

    @Transactional(readOnly = true)
    public Album buscarEntidadePorId(Long idAlbum) {
        validarIdPositivo(
                idAlbum,
                "O ID do álbum deve ser válido."
        );

        return albumRepository
                .findById(idAlbum)
                .orElseThrow(
                        () -> new AlbumNaoEncontradoException(idAlbum)
                );
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

        Album album = buscarEntidadePorId(idAlbum);

        boolean pertenceAoArtista = Objects.equals(
                album.getArtista().getIdArtista(),
                artistaPrincipal.getIdArtista()
        );

        if (!pertenceAoArtista) {
            throw new DadosAlbumInvalidosException(
                    "O álbum selecionado não pertence ao artista principal da música."
            );
        }

        return album;
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

        Short anoLancamento = request.anoLancamento();

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

        if (albumJaExiste) {
            throw new AlbumDuplicadoException(
                    "Já existe um álbum com o título '"
                            + titulo
                            + "' para esse artista no ano de "
                            + anoLancamento
                            + "."
            );
        }
    }

    private void validarIdPositivo(Long id, String mensagem) {
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
