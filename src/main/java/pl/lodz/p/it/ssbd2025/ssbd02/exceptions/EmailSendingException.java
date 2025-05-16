package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import jakarta.mail.MessagingException;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(MessagingException e) {
        super("Email sending error " + e);
    }
}
