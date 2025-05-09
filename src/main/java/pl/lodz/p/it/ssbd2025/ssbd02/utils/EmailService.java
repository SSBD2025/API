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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.util.FileCopyUtils;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.EmailTemplateLoadingException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class EmailService {

    private final JavaMailSender mailSender;

    private final ResourceLoader resourceLoader;

    @Value("${spring.mail.username}")
    private String senderEmail;

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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    @Async
    public void sendResetPasswordEmail(String to, String username, Language language, String token) {
        String mailContent = "<p> Hi, "+ username + ", </p>"+
            "<p><b>You recently requested to reset your password,</b>"+"" +
            "Please, follow the link below to complete the action.</p>"+
            "<a href=\"http://localhost:8080/api/account/reset/password/" +token+ "\">Reset password</a>"+
            "<p> Users Registration Portal Service"; //TODO zmienić link

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
//            helper.setSubject(I18n.getMessage("email.reset.subject", language));
            helper.setSubject("Reset password");
            helper.setText(mailContent, true);

//            Context context = new Context();
//            context.setVariable("welcome", I18n.getMessage("email.welcome", language));
//            context.setVariable("name", username);
//            context.setVariable("body", I18n.getMessage("email.block.body", language));
//            //TODO co się  dzieje z językiem?
//
//
//            String htmlContent = templateEngine.process("emailTemplate", context);
//
//            helper.setText(htmlContent, true);


            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            //TODO
            e.printStackTrace();
        }
    }

    private String loadTemplate(String templateName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/" + templateName);
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace(); //TODO;
            throw new EmailTemplateLoadingException("TODO");
        }
    }

    @Async
    public void sendPasswordChangedByAdminEmail(String to, String username, Language language, String token, String password) {
        String mailContent = "<p> Hi, "+ username + ", </p>"+
                "<p><b>Admin changed your password recently,</b>"+"" +
                "You can chamge it by following this link.</p>"+
                "<a href=\"http://localhost:8080/api/account/reset/password/" +token+ "\">Reset password</a>"+
                "<p> You can also change it manually. Your new, temporary password: <b>" + password + "</b></p>"+
                "<p> Users Registration Portal Service"; //TODO zmienić link

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
//            helper.setSubject(I18n.getMessage("email.reset.subject", language));
            helper.setSubject("Admin changed your password");
            helper.setText(mailContent, true);

//            Context context = new Context();
//            context.setVariable("welcome", I18n.getMessage("email.welcome", language));
//            context.setVariable("name", username);
//            context.setVariable("body", I18n.getMessage("email.block.body", language));
//            //TODO co się  dzieje z językiem?
//
//
//            String htmlContent = templateEngine.process("emailTemplate", context);
//
//            helper.setText(htmlContent, true);


            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            //TODO
            e.printStackTrace();
        }
    }

    @Async
    public void sendActivationMail(String to, String username, String verificationURL, Language language, String token, boolean reminder) { //todo reminder email
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
            e.printStackTrace(); //todo
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
            e.printStackTrace(); //todo
        }
    }
}
