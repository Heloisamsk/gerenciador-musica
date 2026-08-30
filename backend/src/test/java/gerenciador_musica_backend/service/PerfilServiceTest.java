package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.AtualizarPerfilRequestDTO;
import gerenciador_musica_backend.dto.PerfilResponseDTO;
import gerenciador_musica_backend.exception.DadosPerfilInvalidosException;
import gerenciador_musica_backend.model.Artista;
import gerenciador_musica_backend.model.Perfil;
import gerenciador_musica_backend.model.Role;
import gerenciador_musica_backend.model.TipoDestaquePerfil;
import gerenciador_musica_backend.model.Usuario;
import gerenciador_musica_backend.repository.AlbumRepository;
import gerenciador_musica_backend.repository.ArtistaRepository;
import gerenciador_musica_backend.repository.MusicaRepository;
import gerenciador_musica_backend.repository.PerfilRepository;
import gerenciador_musica_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    private PerfilRepository perfilRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ArtistaRepository artistaRepository;
    @Mock
    private MusicaRepository musicaRepository;
    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private PerfilService perfilService;

    private Usuario usuario;
    private Perfil perfil;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(
                "Ana Liz",
                "ana@example.com",
                "hash",
                Role.USER
        );
        ReflectionTestUtils.setField(usuario, "id", 7L);
        perfil = new Perfil(usuario);

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        when(perfilRepository.findByUsuario_Id(7L))
                .thenReturn(Optional.of(perfil));
    }

    @Test
    void deveAtualizarImagensIdentidadeECuradoria() {
        Artista artistaDestaque = new Artista(
                "Marina Sena",
                "Marina de Oliveira Sena",
                "Cantora brasileira.",
                "https://exemplo.com/artista.jpg"
        );
        ReflectionTestUtils.setField(artistaDestaque, "idArtista", 5L);
        Artista artistaFavorita = new Artista(
                "Luedji Luna",
                "Luedji Gomes Santa Rita",
                "Cantora e compositora brasileira.",
                "https://exemplo.com/luedji.jpg"
        );
        ReflectionTestUtils.setField(artistaFavorita, "idArtista", 6L);
        when(artistaRepository.findById(5L))
                .thenReturn(Optional.of(artistaDestaque));
        when(artistaRepository.findById(6L))
                .thenReturn(Optional.of(artistaFavorita));

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "  Ana  ",
                "  analiz  ",
                "  https://exemplo.com/foto.jpg  ",
                "https://exemplo.com/banner.jpg",
                "  Pop brasileiro e música independente.  ",
                "Uma trilha para cada fase.",
                5L,
                null,
                null,
                TipoDestaquePerfil.ARTISTA,
                List.of(6L),
                List.of(),
                List.of()
        );

        PerfilResponseDTO resposta = perfilService.atualizarPerfil(
                usuario,
                request
        );

        assertThat(resposta.nome()).isEqualTo("Ana");
        assertThat(resposta.username()).isEqualTo("analiz");
        assertThat(resposta.bannerUrl())
                .isEqualTo("https://exemplo.com/banner.jpg");
        assertThat(resposta.tipoDestaquePrincipal())
                .isEqualTo(TipoDestaquePerfil.ARTISTA);
        assertThat(resposta.artistaDestaque().titulo())
                .isEqualTo("Marina Sena");
        assertThat(resposta.artistasFavoritos())
                .extracting(item -> item.titulo())
                .containsExactly("Luedji Luna");
        verify(perfilRepository).save(perfil);
    }

    @Test
    void deveRecusarDestaquePrincipalSemItemCorrespondente() {
        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                TipoDestaquePerfil.ALBUM,
                List.of(),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> perfilService.atualizarPerfil(usuario, request))
                .isInstanceOf(DadosPerfilInvalidosException.class)
                .hasMessageContaining("destaque principal");
    }

    @Test
    void deveRecusarDestaqueRepetidoNosFavoritos() {
        Artista artista = new Artista(
                "Marina Sena",
                "Marina de Oliveira Sena",
                "Cantora brasileira.",
                null
        );
        ReflectionTestUtils.setField(artista, "idArtista", 5L);
        when(artistaRepository.findById(5L)).thenReturn(Optional.of(artista));

        AtualizarPerfilRequestDTO request = new AtualizarPerfilRequestDTO(
                "Ana",
                null,
                null,
                null,
                null,
                null,
                5L,
                null,
                null,
                TipoDestaquePerfil.ARTISTA,
                List.of(5L),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> perfilService.atualizarPerfil(usuario, request))
                .isInstanceOf(DadosPerfilInvalidosException.class)
                .hasMessageContaining("não pode ser repetido");
    }

    @Test
    void deveEscolherAutomaticamenteOPrimeiroFavorito() {
        Artista artista = new Artista(
                "Marina Sena",
                "Marina de Oliveira Sena",
                "Cantora brasileira.",
                null
        );
        ReflectionTestUtils.setField(artista, "idArtista", 5L);
        perfil.setArtistaDestaque(artista);

        PerfilResponseDTO resposta = perfilService.obterPerfil(usuario);

        assertThat(resposta.tipoDestaquePrincipal())
                .isEqualTo(TipoDestaquePerfil.ARTISTA);
    }
}
