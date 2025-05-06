package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;

@Component
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

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

}
