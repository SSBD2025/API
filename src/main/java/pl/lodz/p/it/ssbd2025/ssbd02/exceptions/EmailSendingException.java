package pl.lodz.p.it.ssbd2025.ssbd02.exceptions;

import jakarta.mail.MessagingException;
import org.springframework.mail.MailSendException;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(Throwable cause) {
        super("An error occurred while sending the email ", cause);
    }
}
