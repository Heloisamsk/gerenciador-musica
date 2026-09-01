package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AlbumCatalogoDTO;
import gerenciador_musica_backend.dto.ArtistaCatalogoResumoDTO;
import gerenciador_musica_backend.dto.ArtistaDetalheDTO;
import gerenciador_musica_backend.dto.ArtistaRequestDTO;
import gerenciador_musica_backend.dto.ArtistaResponseDTO;
import gerenciador_musica_backend.dto.MusicaCatalogoDTO;
import gerenciador_musica_backend.exception.ArtistaDuplicadoException;
import gerenciador_musica_backend.exception.ArtistaEmUsoException;
import gerenciador_musica_backend.exception.ArtistaNaoEncontradoException;
import gerenciador_musica_backend.exception.DadosArtistaInvalidosException;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.CurtidaAlbumRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.projection.AlbumCatalogoProjection;
import gerenciador_musica_backend.repository.projection.ArtistaCatalogoResumoProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ArtistaService {

    private final ArtistaRepository artistaRepository;
    private final AlbumRepository albumRepository;
    private final MusicaRepository musicaRepository;
    private final CurtidaAlbumRepository curtidaAlbumRepository;

    public ArtistaService(
            ArtistaRepository artistaRepository,
            AlbumRepository albumRepository,
            MusicaRepository musicaRepository,
            CurtidaAlbumRepository curtidaAlbumRepository
    ) {
        this.artistaRepository = artistaRepository;
        this.albumRepository = albumRepository;
        this.musicaRepository = musicaRepository;
        this.curtidaAlbumRepository = curtidaAlbumRepository;
    }

    @Transactional
    public ArtistaResponseDTO cadastrarArtista(ArtistaRequestDTO request){
        validarRequest(request);


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

    @Transactional
    public ArtistaResponseDTO atualizarArtista(
            Long idArtista,
            ArtistaRequestDTO request
    ) {
        validarRequest(request);

        Artista artista = obterEntidadePorId(idArtista);

        String nomeNormalizado = normalizarCampoObrigatorio(
                request.nome(),
                "Nome Artístico"
        );
        String nomeCompletoNormalizado = normalizarCampoObrigatorio(
                request.nomeCompleto(),
                "Nome Completo"
        );
        String descricaoNormalizada = normalizarCampoObrigatorio(
                request.descricao(),
                "Descrição do Artista"
        );
        String fotoPerfilUrlNormalizada = normalizarFotoPerfilUrl(
                request.fotoPerfilUrl()
        );

        verificarDuplicidade(nomeNormalizado, idArtista);

        artista.setNome(nomeNormalizado);
        artista.setNomeCompleto(nomeCompletoNormalizado);
        artista.setDescricao(descricaoNormalizada);
        artista.setFotoPerfilUrl(fotoPerfilUrlNormalizada);

        return converterParaResponse(artista);
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
    public List<ArtistaResponseDTO> buscarPorNome(String termo, int limite) {
        if (termo == null || termo.isBlank()) {
            return List.of();
        }

        String termoNormalizado = termo.strip();

        return artistaRepository
                .findByNomeContainingIgnoreCaseOrNomeCompletoContainingIgnoreCase(
                        termoNormalizado,
                        termoNormalizado,
                        PageRequest.of(0, limite, Sort.by(Sort.Direction.ASC, "nome"))
                )
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArtistaResponseDTO buscarPorId(Long idArtista) {
        Artista artista = obterEntidadePorId(idArtista);

        return converterParaResponse(artista);
    }

    @Transactional(readOnly = true)
    public ArtistaDetalheDTO buscarDetalhesCatalogo(Long idArtista) {
        validarIdArtista(idArtista);

        ArtistaCatalogoResumoProjection resumo = artistaRepository
                .buscarResumoCatalogo(idArtista)
                .orElseThrow(
                        () -> new ArtistaNaoEncontradoException(idArtista)
                );

        List<AlbumCatalogoProjection> albunsProjecao = albumRepository
                .buscarCatalogoPorArtista(idArtista);

        List<Long> idsAlbuns = albunsProjecao.stream()
                .map(AlbumCatalogoProjection::getIdAlbum)
                .toList();

        Long usuarioId = obterUsuarioAutenticado().getId();

        Set<Long> idsAlbunsCurtidos = idsAlbuns.isEmpty()
                ? Set.of()
                : curtidaAlbumRepository.buscarIdsCurtidosPeloUsuario(
                        usuarioId,
                        idsAlbuns
                );

        List<AlbumCatalogoDTO> albuns = albunsProjecao.stream()
                .map(album -> CatalogoProjectionMapper.converterAlbum(
                        album,
                        idsAlbunsCurtidos.contains(album.getIdAlbum())
                ))
                .toList();

        List<MusicaCatalogoDTO> musicas = musicaRepository
                .buscarCatalogoPorArtista(idArtista)
                .stream()
                .map(CatalogoProjectionMapper::converterMusica)
                .toList();

        return new ArtistaDetalheDTO(
                converterResumoCatalogo(resumo),
                albuns,
                musicas
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

        validarDuplicidade(artistaJaExiste, nome);
    }

    private void verificarDuplicidade(
            String nome,
            Long idArtista
    ) {
        boolean artistaJaExiste = artistaRepository
                .existsByNomeIgnoreCaseAndIdArtistaNot(
                        nome,
                        idArtista
                );

        validarDuplicidade(artistaJaExiste, nome);
    }

    private void validarDuplicidade(
            boolean artistaJaExiste,
            String nome
    ) {
        if (artistaJaExiste) {
            throw new ArtistaDuplicadoException(
                    "Esse artista já foi cadastrado: " + nome
            );
        }
    }

    private void validarRequest(ArtistaRequestDTO request) {
        if (request == null) {
            throw new DadosArtistaInvalidosException(
                    "Os dados do artista são obrigatórios"
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

    private ArtistaCatalogoResumoDTO converterResumoCatalogo(
            ArtistaCatalogoResumoProjection resumo
    ) {
        return new ArtistaCatalogoResumoDTO(
                resumo.getIdArtista(),
                resumo.getNome(),
                resumo.getNomeCompleto(),
                resumo.getDescricao(),
                resumo.getFotoPerfilUrl(),
                resumo.getTotalAlbuns(),
                resumo.getTotalMusicasPrincipais(),
                resumo.getTotalParticipacoes(),
                resumo.getDuracaoTotalSegundos()
        );
    }

    @Transactional(readOnly = true)
    public Artista buscarEntidadePorId(Long idArtista) {
        return obterEntidadePorId(idArtista);
    }

    @Transactional
    public void excluirArtista(Long idArtista) {
        Artista artista = obterEntidadePorId(idArtista);

        verificarDependencias(idArtista);

        /*
         * A FK perfil.id_artista_destaque usa ON DELETE SET NULL.
         * Assim, o perfil é preservado e apenas o destaque é removido.
         */
        artistaRepository.delete(artista);
    }

    private void verificarDependencias(Long idArtista) {
        if (albumRepository.existsByArtista_IdArtista(idArtista)) {
            throw new ArtistaEmUsoException(
                    "Não é possível excluir o artista porque "
                            + "ele possui álbuns associados."
            );
        }

        if (musicaRepository
                .existsByArtistaPrincipal_IdArtista(idArtista)) {
            throw new ArtistaEmUsoException(
                    "Não é possível excluir o artista porque ele é "
                            + "o artista principal de uma ou mais músicas."
            );
        }

        if (musicaRepository
                .existsByArtistasParticipantes_IdArtista(idArtista)) {
            throw new ArtistaEmUsoException(
                    "Não é possível excluir o artista porque ele "
                            + "participa de uma ou mais músicas."
            );
        }
    }

    private Artista obterEntidadePorId(Long idArtista) {
        validarIdArtista(idArtista);

        return artistaRepository
                .findById(idArtista)
                .orElseThrow(
                        () -> new ArtistaNaoEncontradoException(idArtista)
                );
    }

    private void validarIdArtista(Long idArtista) {
        if (idArtista == null || idArtista <= 0) {
            throw new DadosArtistaInvalidosException(
                    "O ID do artista deve ser positivo."
            );
        }
    }
}
