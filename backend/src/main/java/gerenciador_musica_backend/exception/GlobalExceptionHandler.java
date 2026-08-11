package gerenciador_musica_backend.exception;

import gerenciador_musica_backend.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /*
     * 400 - Dados da música inválidos segundo
     * as regras de negócio do MusicaService.
     */
    @ExceptionHandler(DadosMusicaInvalidosException.class)
    public ResponseEntity<ErrorResponseDTO> tratarDadosMusicaInvalidos(
            DadosMusicaInvalidosException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /*
     * 400 - Erros encontrados pelas anotações
     * @NotBlank, @NotNull, @Positive, @Size etc.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        fieldErrors.putIfAbsent(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Existem dados inválidos na requisição.",
                request.getRequestURI(),
                fieldErrors
        );
    }

    /*
     * 400 - JSON incorreto, campo com tipo errado,
     * vírgula faltando, enum inválido etc.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> tratarJsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente ou possui formato inválido.",
                request.getRequestURI(),
                Map.of()
        );
    }

    /*
     * 401 - E-mail ou senha incorretos.
     */
    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorResponseDTO> tratarCredenciaisInvalidas(
            CredenciaisInvalidasException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /*
     * 403 - Usuário autenticado, mas sem a Role necessária.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> tratarAcessoNegado(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.FORBIDDEN,
                "Você não possui permissão para realizar esta operação.",
                request.getRequestURI(),
                Map.of()
        );
    }

    /*
     * 404 - Música não encontrada.
     */
    @ExceptionHandler(MusicaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> tratarMusicaNaoEncontrada(
            MusicaNaoEncontradaException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /*
     * 409 - Tentativa de cadastrar a mesma música novamente.
     */
    @ExceptionHandler(MusicaDuplicadaException.class)
    public ResponseEntity<ErrorResponseDTO> tratarMusicaDuplicada(
            MusicaDuplicadaException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /*
     * 409 - Tentativa de cadastrar e-mail já existente.
     */
    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErrorResponseDTO> tratarEmailJaCadastrado(
            EmailJaCadastradoException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /*
     * 500 - Qualquer erro inesperado que não tenha
     * um tratamento específico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> tratarErroInesperado(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "Erro inesperado ao processar {}",
                request.getRequestURI(),
                exception
        );

        return criarResposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor.",
                request.getRequestURI(),
                Map.of()
        );
    }

    private ResponseEntity<ErrorResponseDTO> criarResposta(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                OffsetDateTime.now(),
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