package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.RelatorioAlbumDTO;
import gerenciador_musica_backend.dto.RelatorioArtistaDTO;
import gerenciador_musica_backend.dto.RelatorioCatalogoDTO;
import gerenciador_musica_backend.dto.ResumoCatalogoDTO;
import gerenciador_musica_backend.repository.RelatorioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void deveReunirResumoArtistasEAlbunsNoRelatorio() {
        ResumoCatalogoDTO resumo = new ResumoCatalogoDTO(
                1,
                1,
                2,
                1,
                420
        );
        RelatorioArtistaDTO artista = new RelatorioArtistaDTO(
                1L,
                "Queen",
                1,
                2,
                1,
                420
        );
        RelatorioAlbumDTO album = new RelatorioAlbumDTO(
                1L,
                "A Night at the Opera",
                "Queen",
                (short) 1975,
                2,
                420
        );

        when(relatorioRepository.buscarResumoCatalogo())
                .thenReturn(resumo);
        when(relatorioRepository.listarArtistas())
                .thenReturn(List.of(artista));
        when(relatorioRepository.listarAlbuns())
                .thenReturn(List.of(album));

        RelatorioCatalogoDTO resultado =
                relatorioService.gerarRelatorioCatalogo();

        assertThat(resultado.geradoEm()).isNotNull();
        assertThat(resultado.resumo()).isEqualTo(resumo);
        assertThat(resultado.artistas()).containsExactly(artista);
        assertThat(resultado.albuns()).containsExactly(album);
        verify(relatorioRepository).buscarResumoCatalogo();
        verify(relatorioRepository).listarArtistas();
        verify(relatorioRepository).listarAlbuns();
    }
}
