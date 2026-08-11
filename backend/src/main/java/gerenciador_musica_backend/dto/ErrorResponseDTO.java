package gerenciador_musica_backend.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public ErrorResponseDTO(
            OffsetDateTime timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
        this(
                timestamp,
                status,
                error,
                message,
                path,
                Map.of()
        );
    }

    public ErrorResponseDTO {
        fieldErrors = fieldErrors == null
                ? Map.of()
                : Map.copyOf(fieldErrors);
    }
}