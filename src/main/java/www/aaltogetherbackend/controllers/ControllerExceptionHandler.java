package www.aaltogetherbackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import www.aaltogetherbackend.payloads.responses.ErrorMessageResponse;

import java.util.Objects;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        String field = ex.getBindingResult().getFieldError().getField();
        return ResponseEntity.badRequest().body(new ErrorMessageResponse(message, field));
    }
}
