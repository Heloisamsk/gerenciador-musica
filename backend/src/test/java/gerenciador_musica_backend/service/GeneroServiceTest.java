package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.GeneroResumoDTO;
import gerenciador_musica_backend.model.Genero;
import gerenciador_musica_backend.repository.GeneroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/*
 * Teste de unidade do GeneroService. O GeneroRepository é mockado,
 * portanto os testes não acessam um banco de dados real.
 */
@ExtendWith(MockitoExtension.class)
class GeneroServiceTest {

    @Mock
    private GeneroRepository generoRepository;

    @InjectMocks
    private GeneroService generoService;

    @Test
    void deveListarGenerosCadastrados() {
        Genero genero = new Genero("Rock");
        genero.setIdGenero(1L);

        when(generoRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(genero));

        List<GeneroResumoDTO> resultado = generoService.listarGeneros();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().nome()).isEqualTo("Rock");
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaGenerosCadastrados() {
        when(generoRepository.findAll(any(Sort.class)))
                .thenReturn(List.of());

        List<GeneroResumoDTO> resultado = generoService.listarGeneros();

        assertThat(resultado).isEmpty();
    }
}
