package gerenciador_musica_backend.exception;

import gerenciador_musica_backend.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Monta a resposta padrão de erro da API. Extraído do antigo
 * GlobalExceptionHandler para ser reaproveitado pelos handlers
 * de exceção específicos de cada domínio (Musica, Album, Artista,
 * Review, Playlist, Usuario), em vez de cada um duplicar a mesma
 * lógica de montagem do ErrorResponseDTO.
 *
 * É uma classe utilitária estática (sem estado, sem dependências)
 * em vez de um bean gerenciado pelo Spring: um {@code @Component}
 * aqui não seria enxergado pelo contexto reduzido do
 * {@code @WebMvcTest} de cada controller, já que esse slice só
 * registra automaticamente beans de camada web
 * (@Controller/@ControllerAdvice/conversores/filtros), não
 * qualquer @Component genérico.
 */
public final class ErrorResponseFactory {

    private ErrorResponseFactory() {
    }

    public static ResponseEntity<ErrorResponseDTO> criar(
            HttpStatus status,
            String message,
            String path
    ) {
        return criar(status, message, path, Map.of());
    }

    public static ResponseEntity<ErrorResponseDTO> criar(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                OffsetDateTime.now(ZoneOffset.UTC),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors
        );

        return ResponseEntity
                .status(status)
                .body(resposta);
    }
}
