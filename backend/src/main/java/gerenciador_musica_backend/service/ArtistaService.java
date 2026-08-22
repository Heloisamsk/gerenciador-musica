package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.ArtistaRequestDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.exception.ArtistaDuplicadoException;
import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosArtistaInvalidosException;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.repository.ArtistaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArtistaService {

    private final ArtistaRepository artistaRepository;

    public ArtistaService(ArtistaRepository artistaRepository) {
        this.artistaRepository = artistaRepository;
    }

    @Transactional
    public ArtistaResponseDTO cadastrarArtista(ArtistaRequestDTO request){
        if (request == null) {
            throw new DadosArtistaInvalidosException(
                    "Os dados do artista são obrigatórios"
            );
        }


        String nomeNormalizado = normalizarCampoObrigatorio(
                request.nome(),
                "Nome Artístico");
        String nomeCompletoNormalizado = normalizarCampoObrigatorio(
                request.nomeCompleto(),
                "Nome Completo");
        String descricaoNormalizada = normalizarCampoObrigatorio(
                request.descricao(),
                "Descrição do Artista");
        String fotoPerfilUrlNormalizada = normalizarFotoPerfilUrl(
                request.fotoPerfilUrl()
        );


        verificarDuplicidade(nomeNormalizado);


        Artista artista = new Artista(
                nomeNormalizado,
                nomeCompletoNormalizado,
                descricaoNormalizada,
                fotoPerfilUrlNormalizada
        );


        Artista artistaSalvo =
                artistaRepository.save(artista);


        return converterParaResponse(artistaSalvo);
    }

    @Transactional(readOnly = true)
    public List<ArtistaResponseDTO> listarArtistas() {
        return artistaRepository
                .findAll(Sort.by(Sort.Direction.ASC, "nome"))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArtistaResponseDTO buscarPorId(Long idArtista) {
        Artista artista = obterEntidadePorId(idArtista);

        return converterParaResponse(artista);
    }

    private String normalizarCampoObrigatorio(String valor, String nomeDoCampo){
        if (valor == null) {
            throw new DadosArtistaInvalidosException(
                    nomeDoCampo + " é obrigatório"
            );
        }

        String valorNormalizado = valor
                .strip()
                .replaceAll("\\s+", " ");

        if (valorNormalizado.isBlank()) {
            throw new DadosArtistaInvalidosException(
                    nomeDoCampo + " não pode ficar vazio"
            );
        }

        return valorNormalizado;
    }

    private String normalizarFotoPerfilUrl(String fotoPerfilUrl){
        if (fotoPerfilUrl == null){
            return null;
        }

        String fotoPerfilUrlNormalizada = fotoPerfilUrl.trim();

        if (fotoPerfilUrlNormalizada.isBlank()) {
            return null;
        }

        return fotoPerfilUrlNormalizada;
    }

    private void verificarDuplicidade(String nome){
        boolean artistaJaExiste = artistaRepository.existsByNomeIgnoreCase(
                nome
        );

        if (artistaJaExiste) {
            throw new ArtistaDuplicadoException(
                    "Esse artista já foi cadastrado: " + nome
            );
        }
    }

    private ArtistaResponseDTO converterParaResponse(Artista artista){
        return new ArtistaResponseDTO(
                artista.getIdArtista(),
                artista.getNome(),
                artista.getNomeCompleto(),
                artista.getDescricao(),
                artista.getFotoPerfilUrl()
        );
    }

    @Transactional(readOnly = true)
    public Artista buscarEntidadePorId(Long idArtista) {
        return obterEntidadePorId(idArtista);
    }

    private Artista obterEntidadePorId(Long idArtista) {
        if (idArtista == null || idArtista <= 0) {
            throw new DadosArtistaInvalidosException(
                    "O ID do artista deve ser positivo."
            );
        }

        return artistaRepository
                .findById(idArtista)
                .orElseThrow(
                        () -> new ArtistaNaoEncontradoException(idArtista)
                );
    }
}
