package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.GeneroResumoDTO;
import gerenciador_musica_backend.repository.GeneroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GeneroService {

    private final GeneroRepository generoRepository;

    public GeneroService(GeneroRepository generoRepository) {
        this.generoRepository = generoRepository;
    }

    @Transactional(readOnly = true)
    public List<GeneroResumoDTO> listarGeneros() {
        return generoRepository
                .findAll(Sort.by(Sort.Direction.ASC, "nome"))
                .stream()
                .map(genero -> new GeneroResumoDTO(
                        genero.getIdGenero(),
                        genero.getNome()
                ))
                .toList();
    }
}
