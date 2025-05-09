package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;

@RequiredArgsConstructor
@Component
public class EmailService {

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Async
    public void sendBlockAccountEmail(String to, String username, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.block.subject", language));

            Context context = new Context();
            context.setVariable("welcome", I18n.getMessage("email.welcome", language));
            context.setVariable("name", username);
            context.setVariable("body", I18n.getMessage("email.block.body", language));


            String htmlContent = templateEngine.process("emailTemplate", context);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            //TODO
            e.printStackTrace();
        }
    }

    @Async
    public void sendChangeEmail(String username, String receiver, String confirmationURL, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(receiver);
            helper.setSubject(I18n.getMessage("email.change.subject", language));

            Context context = new Context();
            context.setVariable("welcome", I18n.getMessage("email.welcome", language));
            context.setVariable("name", username);
            context.setVariable("body", I18n.getMessage("email.change.body", language));
            context.setVariable("url", confirmationURL);
            context.setVariable("linkText", I18n.getMessage("email.change.link", language));

            String htmlContent = templateEngine.process("changeEmailTemplate", context);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendRevertChangeEmail(String username, String receiver, String revertChangeURL, Language language) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(receiver);
            helper.setSubject(I18n.getMessage("email.revert.change.subject", language));

            Context context = new Context();
            context.setVariable("welcome", I18n.getMessage("email.welcome", language));
            context.setVariable("name", username);
            context.setVariable("body", I18n.getMessage("email.revert.change.body", language));
            context.setVariable("url", revertChangeURL);
            context.setVariable("linkText", I18n.getMessage("email.revert.change.link", language));

            String htmlContent = templateEngine.process("changeEmailTemplate", context);

            helper.setText(htmlContent, true);

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
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.verification.subject", language));

            Context context = new Context();
            context.setVariable("welcome", I18n.getMessage("email.welcome", language));
            context.setVariable("name", username);
            context.setVariable("body", I18n.getMessage("email.verification.body", language));
            context.setVariable("url", verificationURL+token);
            context.setVariable("linkText", I18n.getMessage("email.verification.link", language));

            String htmlContent = templateEngine.process("changeEmailTemplate", context);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); //todo
        }
    }


    @Async
    public void sendAdminLoginEmail(String to, String username, String IP, Language language){
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(I18n.getMessage("email.login_as_admin.subject", language));

            Context context = new Context();
            context.setVariable("welcome", I18n.getMessage("email.welcome", language));
            context.setVariable("name", username);
            context.setVariable("body", I18n.getMessage("email.login_as_admin.body", language));
            context.setVariable("ip_addr", IP);

            String htmlContent = templateEngine.process("changeEmailTemplate", context);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); //todo
        }
    }
}
