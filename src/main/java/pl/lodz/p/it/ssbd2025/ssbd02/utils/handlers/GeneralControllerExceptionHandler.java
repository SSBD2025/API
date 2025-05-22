package pl.lodz.p.it.ssbd2025.ssbd02.utils.handlers;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.AppBaseException;

import org.springframework.security.authorization.AuthorizationDeniedException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.EmailSendingException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.UnknownFilterException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenBaseException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.TokenSignatureInvalidException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.LoginLoggerInterceptor;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GeneralControllerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GeneralControllerExceptionHandler.class);

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
    public ResponseEntity<ValidationErrorResponse> onConstraintValidationException(
            ConstraintViolationException e) {
        ValidationErrorResponse error = new ValidationErrorResponse();
        e.getConstraintViolations().forEach(
                violation -> {error.getViolations().add(
                        new ValidationErrorResponse.Violation(violation.getPropertyPath().toString(), violation.getMessage()));
                }
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'. Expected '%s' type.",
                invalidValue, paramName, ex.getRequiredType().getSimpleName());
        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.getViolations().add(
                new ValidationErrorResponse.Violation(paramName, errorMessage)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // requires security starter
    // For method level authorization exceptions handling
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleAuthorizationException(
            RuntimeException exception,
            WebRequest webRequest,
            HttpServletRequest request
    ){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        String formattedDate = ZonedDateTime.now().format(formatter);
        String calledBy = request.getUserPrincipal()!=null ? request.getUserPrincipal().getName() : "--ANONYMOUS--";
        String toLog = "[AUTH LOGGER] [" + formattedDate + "] User: " + calledBy + " has attempted to access a resource "+ request.getRequestURL().toString() +" that they do not have permission to.";
        log.warn(toLog);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization exception: "+exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            WebRequest request) {
        Throwable cause = ex.getCause();

        if (cause instanceof org.hibernate.exception.ConstraintViolationException constraintViolationException) {
            String violation = constraintViolationException.getMessage();
            if(violation.contains("account_login_key")){
                violation = "this login is already in use";
            }
            if(violation.contains("account_email_key")){
                violation = "this email is already in use";
            }
            return new ResponseEntity<>("Constraint error: " + violation, HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>("A data integrity error occurred: " + ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TokenBaseException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ResponseEntity<Object> handleTokenBaseException(
            TokenBaseException ex,
            WebRequest request
    ) {
        String fullMessage = ex.getMessage();
        String simpleMessage = fullMessage.substring(
                fullMessage.indexOf('"') + 1,
                fullMessage.lastIndexOf('"')
        );
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Authorization exception");
        body.put("message", simpleMessage);
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(UnknownFilterException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ResponseEntity<Object> handleTokenNotFoundException(
            UnknownFilterException ex,
            WebRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unknown authorization exception: "+ex.getMessage());
    }

    @ExceptionHandler(PersistenceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ResponseEntity<Object> handlePersistenceException(
            PersistenceException ex,
            WebRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Persistence exception: "+ex.getMessage());
    }
}
