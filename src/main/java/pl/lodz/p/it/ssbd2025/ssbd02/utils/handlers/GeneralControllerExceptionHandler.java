package pl.lodz.p.it.ssbd2025.ssbd02.utils.handlers;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;

import org.springframework.security.authorization.AuthorizationDeniedException;

@RestControllerAdvice
public class GeneralControllerExceptionHandler {

    // not-so-pretty way to exclude App Exceptions from default handling
    @ExceptionHandler(AppBaseException.class)
    public void passThroughAppExceptions(
            AppBaseException exception,
            WebRequest request
    ){
        throw exception;
    }

    // For handling unreadable messages ie. non-JSON-parsable body
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void handleException(HttpMessageNotReadableException ex) {
    }

    // For better validation error responses (i.e. multiple error descriptions)
    // You can't handle it by try-catch on controller methods, since validation is performed before method is called
    // Comment it out to see how Spring deals with validation errors by default
    // https://reflectoring.io/bean-validation-with-spring-boot/
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onConstraintValidationException(
            ConstraintViolationException e) {
        ValidationErrorResponse error = new ValidationErrorResponse();
        e.getConstraintViolations().forEach(
                violation -> {error.getViolations().add(
                        new ValidationErrorResponse.Violation(violation.getPropertyPath().toString(), violation.getMessage()));
                }
        );

        return error;
    }

    // For better validation error responses (i.e. multiple error descriptions)
    // You can't handle it by try-catch on controller methods, since validation is performed before method is called
    // Comment it out to see how Spring deals with validation errors by default
    // https://reflectoring.io/bean-validation-with-spring-boot/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ValidationErrorResponse error = new ValidationErrorResponse();
        ex.getBindingResult().getFieldErrors().forEach(
                fieldError -> {
                    error.getViolations().add(
                            new ValidationErrorResponse.Violation(fieldError.getField(), fieldError.getDefaultMessage()));
                }
        );
        return ResponseEntity.ofNullable(error);
    }

    // requires security starter
    // For method level authorization exceptions handling
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleAuthorizationException(
            RuntimeException exception,
            WebRequest request
    ){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization exception: "+exception.getMessage());
    }

   // For general unknown exceptions handling
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleAllUncaughtException(
            RuntimeException exception,
            WebRequest request
    ){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception thrown by controller: "+exception);
    }
}