package hexlet.code.handler;

import hexlet.code.exception.ResourceInUseException;
import hexlet.code.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ResourceInUseException.class)
    public ResponseEntity<String> handleInUse(ResourceInUseException e) {
        log.warn("Resource is in use: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data integrity violation");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthentication(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    /**
     * AccessDeniedException прокидывается дальше намеренно: его обрабатывает Spring Security
     * и отдаёт 403. Если перехватить его здесь обработчиком Exception, вернётся 500 вместо 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        throw e;
    }

    /**
     * Последний рубеж: всё, что не попало в обработчики выше.
     * Логируется со стектрейсом и уезжает в Bugsink, наружу уходит нейтральное сообщение
     * без внутренних деталей реализации.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }
}
