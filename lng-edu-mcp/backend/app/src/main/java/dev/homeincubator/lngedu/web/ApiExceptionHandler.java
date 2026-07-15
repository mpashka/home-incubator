package dev.homeincubator.lngedu.web;

import dev.homeincubator.lngedu.common.ForbiddenException;
import dev.homeincubator.lngedu.common.NotFoundException;
import dev.homeincubator.lngedu.common.ValidationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Single RFC 7807 Problem Details mapper for the REST adapter. Keeps transport concerns out of the
 * domain: {@link NotFoundException} -> 404, {@link ValidationException} and bean-validation
 * failures -> 400 (with field details), everything else -> 500. Every response carries a UTC
 * ISO-8601 {@code timestamp} property.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String TYPE_BASE = "urn:problem-type:";

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "not-found", "Not Found", ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidation(ValidationException ex) {
        return problem(HttpStatus.BAD_REQUEST, "validation-error", "Validation Failed", ex.getMessage());
    }

    /** Ownership guard (@tag:auth): the authenticated account does not own the referenced learner. */
    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex) {
        return problem(HttpStatus.FORBIDDEN, "forbidden", "Forbidden", ex.getMessage());
    }

    /** Request-body bean-validation (@Valid on @RequestBody records). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBodyValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(),
                        "message", fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
                .toList();
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "validation-error", "Validation Failed",
                "Request validation failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    /** Query/path parameter bean-validation (@Validated on controllers). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(v -> Map.of("field", v.getPropertyPath().toString(), "message", v.getMessage()))
                .toList();
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "validation-error", "Validation Failed",
                "Request validation failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    /** Malformed query/path parameter (e.g. a non-UUID userId). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = "Parameter '" + ex.getName() + "' has an invalid value";
        return problem(HttpStatus.BAD_REQUEST, "validation-error", "Validation Failed", detail);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Internal Server Error",
                "Unexpected server error");
    }

    private static ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(TYPE_BASE + type));
        pd.setTitle(title);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
