package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

public class EmailTemplateLoadingException extends RuntimeException {
    public EmailTemplateLoadingException(String message) {
        super(message);
    }

    public EmailTemplateLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}

