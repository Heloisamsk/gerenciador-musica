package gerenciador_musica_backend.service;

import gerenciador_musica_backend.dto.RelatorioCatalogoDTO;
import gerenciador_musica_backend.repository.RelatorioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;

    public RelatorioService(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    @Transactional(readOnly = true)
    public RelatorioCatalogoDTO gerarRelatorioCatalogo() {
        return new RelatorioCatalogoDTO(
                OffsetDateTime.now(ZoneOffset.UTC),
                relatorioRepository.buscarResumoCatalogo(),
                relatorioRepository.listarArtistas(),
                relatorioRepository.listarAlbuns()
        );
    }
}
