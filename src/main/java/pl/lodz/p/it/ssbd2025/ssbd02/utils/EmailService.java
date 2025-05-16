package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.util.FileCopyUtils;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.EmailSendingException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.EmailTemplateLoadingException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@MethodCallLogged
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled=true)
@Component
public class EmailService {

    private final JavaMailSender mailSender;

    private final ResourceLoader resourceLoader;

    @Value("${spring.mail.username}")
    private String senderEmail;

//    private static final String RESET_PASSWORD_URL = "http://localhost:5173/reset/password/"; //TODO locally
    private static final String RESET_PASSWORD_URL = "team-2.proj-sum.it.p.lodz.pl/reset/password/"; //TODO on machine

    @Async
    public void sendChangeEmail(String username, String receiver, String confirmationURL, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate("changeEmailTemplate.html")
                    .replace("${welcome}", I18n.getMessage("email.welcome", language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage("email.change.body", language))
                    .replace("${url}", confirmationURL)
                    .replace("${linkText}", I18n.getMessage("email.change.link", language));

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(receiver);
            helper.setSubject(I18n.getMessage("email.change.subject", language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailSendingException(e);
        }
    }

    @Async
    public void sendRevertChangeEmail(String username, String receiver, String revertChangeURL, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate("changeEmailTemplate.html")
                    .replace("${welcome}", I18n.getMessage("email.welcome", language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage("email.revert.change.body", language))
                    .replace("${url}", revertChangeURL)
                    .replace("${linkText}", I18n.getMessage("email.revert.change.link", language));

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(receiver);
            helper.setSubject(I18n.getMessage("email.revert.change.subject", language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new EmailSendingException(e);
        }
    }

    @Async
    public void sendResetPasswordEmail(String to, String username, Language language, String token) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate("changeEmailTemplate.html")
                    .replace("${welcome}", I18n.getMessage("email.welcome", language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage("email.own.reset.body", language))
                    .replace("${url}", RESET_PASSWORD_URL+token)
                    .replace("${linkText}", I18n.getMessage("email.own.reset.link", language));
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.reset.subject", language));
            helper.setText(emailBody, true);
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new EmailSendingException(e);
        }
    }

    @Async
    public void sendPasswordChangedByAdminEmail(String to, String username, Language language, String token, String password) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate("adminChangedPassword.html")
                    .replace("${welcome}", I18n.getMessage("email.welcome", language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage("email.reset.password.body", language))
                    .replace("${url}", RESET_PASSWORD_URL+token)
                    .replace("${linkText}", I18n.getMessage("email.reset.link", language))
                    .replace("${manually}", I18n.getMessage("email.reset.manually", language) + " <b>" + password + "</b>");
            ;
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.reset.subject", language));
            helper.setText(emailBody, true);
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new EmailSendingException(e);
        }
    }

    private String loadTemplate(String templateName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/" + templateName);
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            }
        } catch (IOException | NullPointerException e) {
            throw new EmailTemplateLoadingException("TODO");
        }
    }

    @Async
    @PreAuthorize("permitAll()")
    public void sendActivationMail(String to, String username, String verificationURL, Language language, String token) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate("changeEmailTemplate.html")
                    .replace("${welcome}", I18n.getMessage("email.welcome", language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage("email.verification.body", language))
                    .replace("${url}", verificationURL+token)
                    .replace("${linkText}", I18n.getMessage("email.verification.link", language));

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.verification.subject", language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new EmailSendingException(e);
        }
    }


    @Async
    public void sendAdminLoginEmail(String to, String username, String IP, Language language){
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate("emailAdminLoginTemplate.html")
                    .replace("${welcome}", I18n.getMessage("email.welcome", language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage("email.login_as_admin.body", language))
                    .replace("${ip_addr}", IP);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.login_as_admin.subject", language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new EmailSendingException(e);
        }
    }

    @Async
    public void sendTwoFactorCode(String to, String username, String code, Language language){
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate("twoFactorTemplate.html")
                    .replace("${welcome}", I18n.getMessage("email.welcome", language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage("email.2fa.body", language))
                    .replace("${code}", code);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.2fa.subject", language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new EmailSendingException(e);
        }
    }

    @Async
    @PreAuthorize("permitAll()")
    public void sendAccountDeletedEmail(String to, String username, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate("emailTemplate.html")
                    .replace("${welcome}", I18n.getMessage(I18n.EMAIL_WELCOME, language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage(I18n.EMAIL_DELETE_ACCOUNT_BODY, language));

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.delete.account.subject", language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {

            throw new EmailSendingException(e);
        }
    }

    @Async
    @PreAuthorize("permitAll()")
    public void sendVerificationReminderEmail(String to, String username, String verificationURL, Language language, String token) {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            try {
                String emailBody = loadTemplate("changeEmailTemplate.html")
                        .replace("${welcome}", I18n.getMessage("email.welcome", language))
                        .replace("${name}", username)
                        .replace("${body}", I18n.getMessage(I18n.EMAIL_VERIFICATION_REMINDER_BODY, language))
                        .replace("${url}", verificationURL+token)
                        .replace("${linkText}", I18n.getMessage("email.verification.link", language));

                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(senderEmail);
                helper.setTo(to);
                helper.setSubject(I18n.getMessage(I18n.EMAIL_VERIFICATION_REMINDER_SUBJECT, language));
                helper.setText(emailBody, true);

                mailSender.send(mimeMessage);

            } catch (MessagingException e) {
                throw new EmailSendingException(e);
            }
        }
}
