package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.*;
import gerenciador_musica_backend.exception.DadosMusicaInvalidosException;
import gerenciador_musica_backend.exception.MusicaDuplicadaException;
import gerenciador_musica_backend.exception.MusicaNaoEncontradaException;
import gerenciador_musica_backend.model.Album;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Genero;
import gerenciador_musica_backend.model.Musica;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.GeneroRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MusicaService {

    private final MusicaRepository musicaRepository;
    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;
    private final GeneroRepository generoRepository;

    public MusicaService(MusicaRepository musicaRepository, AlbumRepository albumRepository, ArtistaRepository artistaRepository, GeneroRepository generoRepository) {
        this.musicaRepository = musicaRepository;
        this.albumRepository = albumRepository;
        this.artistaRepository = artistaRepository;
        this.generoRepository = generoRepository;
    }

    @Transactional
    public MusicaResponseDTO cadastrarMusica(MusicaRequestDTO request) {

        validarRegrasDeNegocio(request);

        Artista artistaPrincipal = buscarOuCriarArtista(request.artistaPrincipal());

        Album album = buscarOuCriarAlbum(request.album(), artistaPrincipal);

        verificarDuplicidade(
                request,
                artistaPrincipal,
                album
        );

        Set<Artista> participantes = buscarOuCriarParticipantes(
                request.artistasParticipantes(),
                artistaPrincipal
        );

        Set<Genero> generos = buscarOuCriarGeneros(request.generos());

        Musica musica = montarMusica(
                request,
                artistaPrincipal,
                album,
                participantes,
                generos
        );

        Musica musicaSalva = musicaRepository.save(musica);

        return converterParaResponse(musicaSalva);
    }

    private void validarRegrasDeNegocio(
            MusicaRequestDTO request
    ) {
        if (request == null){
            throw new DadosMusicaInvalidosException(
                    "Os dados da música são obrigatórios."
            );
        }

        ArtistaRequestDTO artistaPrincipal = request.artistaPrincipal();

        if (artistaPrincipal == null){
            throw new DadosMusicaInvalidosException(
                    "O artista principal é obrigatório."
            );
        }

        String nomeArtistaPrincipal =
                normalizarParaComparacao(
                        artistaPrincipal.nome()
                );

        if (nomeArtistaPrincipal.isBlank()) {
            throw new DadosMusicaInvalidosException(
                    "O nome do artista principal é obrigatório"
            );
        }

        String nomeCompletoArtistaPrincipal =
                normalizarParaComparacao(
                        artistaPrincipal.nomeCompleto()
                );

        if (nomeCompletoArtistaPrincipal.isBlank()) {
            throw new DadosMusicaInvalidosException(
                    "O nome completo do artista principal é obrigatório."
            );
        }

        validarParticipantes(
                request.artistasParticipantes(),
                nomeArtistaPrincipal
        );

        validarGeneros(request.generos());

        validarAlbum(request.album());
    }

    private Artista buscarOuCriarArtista(
            ArtistaRequestDTO dadosArtista
    ) {
        String nomeNormalizado =
                normalizarTexto(dadosArtista.nome());

        String nomeCompletoNormalizado =
                normalizarTexto(dadosArtista.nomeCompleto());

        return artistaRepository
                .findByNomeIgnoreCase(nomeNormalizado)
                .orElseGet(() -> {
                    Artista novoArtista = new Artista(
                            nomeNormalizado,
                            nomeCompletoNormalizado,
                            normalizarTextoOpcional(
                                    dadosArtista.descricao()
                            ),
                            normalizarTextoOpcional(
                                    dadosArtista.fotoPerfilUrl()
                            )
                    );

                    return artistaRepository.save(novoArtista);
                });
    }

    private Album buscarOuCriarAlbum(
            AlbumRequestDTO dadosAlbum,
            Artista artistaPrincipal
    ) {

        if (dadosAlbum == null) {
            return null;
        }

        String tituloNormalizado =
                normalizarTexto(dadosAlbum.titulo());

        return albumRepository
                .findByTituloIgnoreCaseAndArtistaAndAnoLancamento(
                        tituloNormalizado,
                        artistaPrincipal,
                        dadosAlbum.anoLancamento()
                )
                .orElseGet(() -> {
                    Album novoAlbum = new Album(
                            artistaPrincipal,
                            tituloNormalizado,
                            dadosAlbum.anoLancamento(),
                            normalizarTextoOpcional(dadosAlbum.capaUrl())
                    );

                    return albumRepository.save(novoAlbum);
                });
    }

    private void verificarDuplicidade(
            MusicaRequestDTO request,
            Artista artistaPrincipal,
            Album album
    ) {

        String tituloNormalizado =
                normalizarTexto(request.titulo());

        boolean musicaJaExiste;

        if (album != null){
            musicaJaExiste =
                    musicaRepository.existsByAlbumAndTituloIgnoreCase(
                            album,
                            tituloNormalizado
                    );
        } else {
            musicaJaExiste =
                    musicaRepository.existsByAlbumIsNullAndArtistaPrincipalAndTituloIgnoreCaseAndAnoLancamento(
                            artistaPrincipal,
                            tituloNormalizado,
                            request.anoLancamento()
                    );
        }

        if(musicaJaExiste) {
            throw new MusicaDuplicadaException(
              "A música já está cadastrada."
            );
        }
    }



    private Set<Artista> buscarOuCriarParticipantes(
            Set<ArtistaRequestDTO> dadosParticipantes,
            Artista artistaPrincipal
    ) {

        Set<Artista> participantes = new LinkedHashSet<>();

        if (dadosParticipantes == null || dadosParticipantes.isEmpty()) {
            return participantes;
        }

        for (ArtistaRequestDTO dadosParticipante : dadosParticipantes) {
            Artista participante = buscarOuCriarArtista(dadosParticipante);

            boolean mesmoArtistaPrincipal =
                    participante
                            .getIdArtista()
                            .equals(artistaPrincipal.getIdArtista());

            if (mesmoArtistaPrincipal) {
                throw new DadosMusicaInvalidosException(
                  "O Artista principal não pode aparecer entre os participantes"
                );
            }

            participantes.add(participante);
        }

        return participantes;
    }

    private Set<Genero> buscarOuCriarGeneros(
            Set<String> nomesGeneros
    ) {
        Set<Genero> generos = new LinkedHashSet<>();

        if (nomesGeneros == null || nomesGeneros.isEmpty()) {
            return generos;
        }

        for (String nomeRecebido : nomesGeneros) {
            String nomeNormalizado =
                    normalizarTexto(nomeRecebido);

            if (nomeNormalizado.isBlank()) {
                throw new DadosMusicaInvalidosException(
                        "O nome do gênero não pode ficar vazio."
                );
            }

            Genero genero = generoRepository
                    .findByNomeIgnoreCase(nomeNormalizado)
                    .orElseGet(() -> {
                        Genero novoGenero =
                                new Genero(nomeNormalizado);

                        return generoRepository.save(novoGenero);
                    });

            generos.add(genero);
        }

        return generos;
    }

    private Musica montarMusica(
            MusicaRequestDTO request,
            Artista artistaPrincipal,
            Album album,
            Set<Artista> artistasParticipantes,
            Set<Genero> generos
    ) {
        String tituloNormalizado =
                normalizarTexto(request.titulo());

        String letraNormalizada =
                normalizarLetra(request.letra());

        return new Musica(
                tituloNormalizado,
                letraNormalizada,
                request.duracaoSegundos(),
                request.anoLancamento(),
                artistaPrincipal,
                album,
                artistasParticipantes,
                generos
        );
    }

    private MusicaResponseDTO converterParaResponse(
            Musica musica
    ) {
        ArtistaResumoDTO artistaPrincipal =
                converterArtistaParaResumo(
                        musica.getArtistaPrincipal()
                );

        AlbumResumoDTO album =
                converterAlbumParaResumo(
                        musica.getAlbum()
                );

        Set<ArtistaResumoDTO> participantes =
                musica.getArtistasParticipantes()
                        .stream()
                        .map(this::converterArtistaParaResumo)
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        Set<GeneroResumoDTO> generos =
                musica.getGeneros()
                        .stream()
                        .map(this::converterGeneroParaResumo)
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        return new MusicaResponseDTO(
                musica.getIdMusica(),
                musica.getTitulo(),
                musica.getLetra(),
                musica.getDuracaoSegundos(),
                musica.getAnoLancamento(),
                artistaPrincipal,
                album,
                participantes,
                generos
        );
    }

    private String normalizarParaComparacao(
            String texto
    ) {
        if (texto == null) {
            return "";
        }

        return texto
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private String normalizarTexto(String texto) {
        return texto
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String normalizarTextoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return normalizarTexto(texto);
    }

    private void validarParticipantes (
            Set<ArtistaRequestDTO> participantes,
            String nomeArtistaPrincipal
    ) {
        if (participantes == null) {
            throw new DadosMusicaInvalidosException(
                    "A lista de artistas participantes é obrigatória."
            );
        }

        Set<String> nomesEncontrados = new HashSet<>();

        for(ArtistaRequestDTO participante : participantes) {

            if(participante == null) {
                throw new DadosMusicaInvalidosException(
                        "A lista possui um participante inválido."
                );
            }

            String nomeParticipante =
                    normalizarParaComparacao(
                            participante.nome()
                    );

            if (nomeParticipante.isBlank()) {
                throw new DadosMusicaInvalidosException(
                        "O nome do artista participante é obrigatório."
                );
            }

            String nomeCompletoParticipante =
                    normalizarParaComparacao(
                            participante.nomeCompleto()
                    );

            if (nomeCompletoParticipante.isBlank()) {
                throw new DadosMusicaInvalidosException(
                        "O nome completo do artista participante é obrigatório."
                );
            }

            if (nomeParticipante.equals(nomeArtistaPrincipal)) {
                throw new DadosMusicaInvalidosException(
                        "O artista principal não pode aparecer como participante."
                );
            }
        }
    }

    private void validarGeneros(
            Set<String> generos
    ) {

        if (generos == null || generos.isEmpty()) {
            throw new DadosMusicaInvalidosException(
                    "A música deve possuir pelo ou menos um gênero."
            );
        }

        Set<String> nomesEncontrados = new HashSet<>();

        for (String genero : generos) {

            String nomeNormalizado =
                    normalizarParaComparacao(genero);

            if (nomeNormalizado.isBlank()) {
                throw new DadosMusicaInvalidosException(
                        "O nome do gênero não pode ser vazio."
                );
            }

            boolean foiAdicionado = nomesEncontrados.add(nomeNormalizado);

            if(!foiAdicionado) {
                throw new DadosMusicaInvalidosException(
                        "O gênero está repetido: " + genero
                );
            }
        }
    }

    private void validarAlbum(
            AlbumRequestDTO album
    ) {
        if (album == null) {
            return;
        }

        String tituloNormalizado =
                normalizarParaComparacao(album.titulo());

        if (tituloNormalizado.isBlank()) {
            throw new DadosMusicaInvalidosException(
                    "O título do álbum é obrigatório."
            );
        }

        if (album.anoLancamento() == null) {
            throw new DadosMusicaInvalidosException(
              "O ano de lançamento do álbum é obrigatório"
            );
        }
    }

    private String normalizarLetra(String letra) {
        if (letra == null || letra.isBlank()) {
            return null;
        }

        return letra.strip();
    }

    private ArtistaResumoDTO converterArtistaParaResumo(
            Artista artista
    ) {
        return new ArtistaResumoDTO(
                artista.getIdArtista(),
                artista.getNome(),
                artista.getNomeCompleto(),
                artista.getDescricao(),
                artista.getFotoPerfilUrl()
        );
    }

    private AlbumResumoDTO converterAlbumParaResumo(
            Album album
    ) {
        if (album == null) {
            return null;
        }

        return new AlbumResumoDTO(
                album.getIdAlbum(),
                album.getTitulo(),
                album.getAnoLancamento(),
                album.getCapaUrl()
        );
    }

    private GeneroResumoDTO converterGeneroParaResumo(
            Genero genero
    ) {
        return new GeneroResumoDTO(
                genero.getIdGenero(),
                genero.getNome()
        );
    }

    @Transactional(readOnly = true)
    public List<MusicaResponseDTO> listarMusicas() {
        return musicaRepository
                .findAll(Sort.by(Sort.Direction.ASC, "titulo"))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MusicaResponseDTO buscarPorId(Long id) {
        Musica musica = musicaRepository
                .findById(id)
                .orElseThrow(
                        () -> new MusicaNaoEncontradaException(id)
                );

        return converterParaResponse(musica);
    }
}



