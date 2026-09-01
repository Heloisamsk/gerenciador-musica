package gerenciador_musica_backend.exception;

import gerenciador_musica_backend.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

        return ErrorResponseFactory.criar(
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
        return ErrorResponseFactory.criar(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente ou possui formato inválido.",
                request.getRequestURI()
        );
    }

    /*
     * 400 - Parâmetro de query/URL com tipo incompatível
     * (ex: ?ano=abc quando o campo espera um número).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> tratarParametroComTipoInvalido(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.BAD_REQUEST,
                "O parâmetro '" + exception.getName() + "' possui um valor inválido.",
                request.getRequestURI()
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
        return ErrorResponseFactory.criar(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request.getRequestURI()
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
        return ErrorResponseFactory.criar(
                HttpStatus.FORBIDDEN,
                "Você não possui permissão para realizar esta operação.",
                request.getRequestURI()
        );
    }

    /*
     * 409 - Violação de restrição de integridade do banco
     * (chave única, chave estrangeira etc.) não tratada por
     * uma exceção de negócio mais específica.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> tratarConflitoBanco(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return ErrorResponseFactory.criar(
                HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados.",
                request.getRequestURI()
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

        return ErrorResponseFactory.criar(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor.",
                request.getRequestURI()
        );
    }
}
