package com.luxuriusroom.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::toString)
            .toList();
        return construirError(HttpStatus.BAD_REQUEST, "Solicitud inválida", "Hay errores en la solicitud", detalles);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return construirError(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage(), List.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return construirError(HttpStatus.UNAUTHORIZED, "No autorizado", ex.getMessage(), List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return construirError(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return construirError(HttpStatus.CONFLICT, "Conflicto", ex.getMessage(), List.of());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        return construirError(HttpStatus.FORBIDDEN, "Acceso prohibido", ex.getMessage(), List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return construirError(HttpStatus.FORBIDDEN, "Acceso prohibido", "No tiene permisos para esta operación", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return construirError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado", List.of());
    }

    private ResponseEntity<Map<String, Object>> construirError(HttpStatus status, String error, String mensaje, List<String> detalles) {
        return ResponseEntity.status(status).body(Map.of(
            "estado", status.value(),
            "error", error,
            "mensaje", mensaje,
            "detalles", detalles
        ));
    }
}
