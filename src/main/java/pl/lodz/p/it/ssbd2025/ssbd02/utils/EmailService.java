package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.EmailSendingException;
import pl.lodz.p.it.ssbd2025.ssbd02.interceptors.MethodCallLogged;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.EmailTemplateLoadingException;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.EmailConsts;

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
    //private static final String RESET_PASSWORD_URL = "https://team-2.proj-sum.it.p.lodz.pl/reset/password/"; //TODO on machine

    @Value("$app.environment}")
    private String environment;

    @PreAuthorize("hasRole('ADMIN')||hasRole('CLIENT')||hasRole('DIETICIAN')")
    @Async
    public void sendChangeEmail(String username, String receiver, String confirmationURL, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate(EmailConsts.TEMPLATE_CHANGE_EMAIL)
                    .replace(EmailConsts.PLACEHOLDER_WELCOME, I18n.getMessage("email.welcome", language))
                    .replace(EmailConsts.PLACEHOLDER_NAME, username)
                    .replace(EmailConsts.PLACEHOLDER_BODY, I18n.getMessage("email.change.body", language))
                    .replace(EmailConsts.PLACEHOLDER_URL, confirmationURL)
                    .replace(EmailConsts.PLACEHOLDER_LINK_TEXT, I18n.getMessage("email.change.link", language));

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(receiver);
            helper.setSubject(I18n.getMessage("email.change.subject", language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException | MailSendException e) {
            throw new EmailSendingException(e);
        }
    }

    @PreAuthorize("permitAll()")
    @Async
    public void sendRevertChangeEmail(String username, String receiver, String revertChangeURL, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate(EmailConsts.TEMPLATE_CHANGE_EMAIL)
                    .replace(EmailConsts.PLACEHOLDER_WELCOME, I18n.getMessage("email.welcome", language))
                    .replace(EmailConsts.PLACEHOLDER_NAME, username)
                    .replace(EmailConsts.PLACEHOLDER_BODY, I18n.getMessage("email.revert.change.body", language))
                    .replace(EmailConsts.PLACEHOLDER_URL, revertChangeURL)
                    .replace(EmailConsts.PLACEHOLDER_LINK_TEXT, I18n.getMessage("email.revert.change.link", language));

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(receiver);
            helper.setSubject(I18n.getMessage("email.revert.change.subject", language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException | MailSendException e) {
            throw new EmailSendingException(e);
        }
    }

    @PreAuthorize("permitAll()")
    @Async
    public void sendResetPasswordEmail(String to, String username, Language language, String token) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String resetUrl = "prod".equals(environment) ? EmailConsts.RESET_PASSWORD_URL_PROD : EmailConsts.RESET_PASSWORD_URL_LOCAL;
            String emailBody = loadTemplate(EmailConsts.TEMPLATE_CHANGE_EMAIL)
                    .replace(EmailConsts.PLACEHOLDER_WELCOME, I18n.getMessage("email.welcome", language))
                    .replace(EmailConsts.PLACEHOLDER_NAME, username)
                    .replace(EmailConsts.PLACEHOLDER_BODY, I18n.getMessage("email.own.reset.body", language))
                    .replace(EmailConsts.PLACEHOLDER_URL, resetUrl + token)
                    .replace(EmailConsts.PLACEHOLDER_LINK_TEXT, I18n.getMessage("email.own.reset.link", language));
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.reset.subject", language));
            helper.setText(emailBody, true);
            mailSender.send(mimeMessage);

        } catch (MessagingException | MailSendException e) {
            throw new EmailSendingException(e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Async
    public void sendBlockAccountEmail(String to, String username, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate(EmailConsts.TEMPLATE_EMAIL)
                    .replace(EmailConsts.PLACEHOLDER_WELCOME, I18n.getMessage(I18n.EMAIL_WELCOME, language))
                    .replace(EmailConsts.PLACEHOLDER_NAME, username)
                    .replace(EmailConsts.PLACEHOLDER_BODY, I18n.getMessage(I18n.EMAIL_BLOCK_BODY, language));

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage(I18n.EMAIL_BLOCK_SUBJECT, language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException | EmailTemplateLoadingException e) {
            throw new EmailSendingException(e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Async
    public void sendPasswordChangedByAdminEmail(String to, String username, Language language, String token, String password) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String resetUrl = "prod".equals(environment) ? EmailConsts.RESET_PASSWORD_URL_PROD : EmailConsts.RESET_PASSWORD_URL_LOCAL;
            String emailBody = loadTemplate(EmailConsts.TEMPLATE_ADMIN_CHANGED_PASSWORD)
                    .replace(EmailConsts.PLACEHOLDER_WELCOME, I18n.getMessage("email.welcome", language))
                    .replace(EmailConsts.PLACEHOLDER_NAME, username)
                    .replace(EmailConsts.PLACEHOLDER_BODY, I18n.getMessage("email.reset.password.body", language))
                    .replace(EmailConsts.PLACEHOLDER_URL, resetUrl + token)
                    .replace(EmailConsts.PLACEHOLDER_LINK_TEXT, I18n.getMessage("email.reset.link", language))
                    .replace(EmailConsts.PLACEHOLDER_MANUALLY, I18n.getMessage("email.reset.manually", language) + " <b>" + password + "</b>");

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.reset.subject", language));
            helper.setText(emailBody, true);
            mailSender.send(mimeMessage);

        } catch (MessagingException | MailSendException e) {
            throw new EmailSendingException(e);
        }
    }

    @PreAuthorize("permitAll()")
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

        } catch (MessagingException | MailSendException e) {
            throw new EmailSendingException(e);
        }
    }

    //TODO co z tym
    @Async
    public void sendAdminLoginEmail(String to, String username, String IP, Language language){
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate(EmailConsts.TEMPLATE_ADMIN_LOGIN)
                    .replace(EmailConsts.PLACEHOLDER_WELCOME, I18n.getMessage("email.welcome", language))
                    .replace(EmailConsts.PLACEHOLDER_NAME, username)
                    .replace(EmailConsts.PLACEHOLDER_BODY, I18n.getMessage("email.login_as_admin.body", language))
                    .replace(EmailConsts.PLACEHOLDER_IP_ADDR, IP);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.login_as_admin.subject", language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException | MailSendException e) {
            throw new EmailSendingException(e);
        }
    }

    @PreAuthorize("permitAll()")
    @Async
    public void sendTwoFactorCode(String to, String username, String code, Language language){
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate(EmailConsts.TEMPLATE_TWO_FACTOR)
                    .replace(EmailConsts.PLACEHOLDER_WELCOME, I18n.getMessage("email.welcome", language))
                    .replace(EmailConsts.PLACEHOLDER_NAME, username)
                    .replace(EmailConsts.PLACEHOLDER_BODY, I18n.getMessage("email.2fa.body", language))
                    .replace(EmailConsts.PLACEHOLDER_CODE, code);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.2fa.subject", language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException | MailSendException e) {
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
            helper.setSubject(I18n.getMessage(I18n.EMAIL_DELETE_ACCOUNT_SUBJECT, language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException | MailSendException e) {

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

        } catch (MessagingException | MailSendException e) {
            throw new EmailSendingException(e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Async
    public void sendUnblockAccountEmail(String to, String username, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate("emailTemplate.html")
                    .replace("${welcome}", I18n.getMessage(I18n.EMAIL_WELCOME, language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage(I18n.EMAIL_UNBLOCK_BODY, language));

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage(I18n.EMAIL_UNBLOCK_SUBJECT, language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException | MailSendException e) {
            throw new EmailSendingException(e);
        }
    }

    @Async
    @PreAuthorize("permitAll()")
    public void sendActivateAccountEmail(String email, String login, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String emailBody = loadTemplate("emailTemplate.html")
                    .replace("${welcome}", I18n.getMessage(I18n.EMAIL_WELCOME, language))
                    .replace("${name}", login)
                    .replace("${body}", I18n.getMessage(I18n.EMAIL_ACTIVATE_ACCOUNT_BODY, language));

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject(I18n.getMessage(I18n.EMAIL_ACTIVATE_ACCOUNT_SUBJECT, language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException | MailSendException e) {
            throw new EmailSendingException(e);
        }
    }

    @Async
    public void sendRoleAssignedEmail(String to, String username, String roleName, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String translatedRoleName = I18n.getMessage("role." + roleName.toLowerCase(), language);
            String emailBody = loadTemplate("emailTemplate.html")
                    .replace("${welcome}", I18n.getMessage(I18n.EMAIL_WELCOME, language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage(I18n.EMAIL_ROLE_ASSIGNED_BODY, language)
                            + " " + translatedRoleName);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage(I18n.EMAIL_ROLE_ASSIGNED_SUBJECT, language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new EmailSendingException(e);
        }
    }

    @Async
    public void sendRoleUnassignedEmail(String to, String username, String roleName, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            String translatedRoleName = I18n.getMessage("role." + roleName.toLowerCase(), language);
            String emailBody = loadTemplate("emailTemplate.html")
                    .replace("${welcome}", I18n.getMessage(I18n.EMAIL_WELCOME, language))
                    .replace("${name}", username)
                    .replace("${body}", I18n.getMessage(I18n.EMAIL_ROLE_UNASSIGNED_BODY, language)
                            + " " + translatedRoleName);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage(I18n.EMAIL_ROLE_UNASSIGNED_SUBJECT, language));
            helper.setText(emailBody, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new EmailSendingException(e);
        }
    }
}
