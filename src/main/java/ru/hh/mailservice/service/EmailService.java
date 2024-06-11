package ru.hh.mailservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.hh.mailservice.model.EmailContext;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String ADDRESS_FROM = "forestofhabits@yandex.ru";
    private static final String LANGUAGE = "RU";
    private static final String NAME_FROM = "Forest Of Habits";
    private static final String SUBJECT = "Регистрация в Forest Of Habits";
    private static final String DISPLAY_NAME = "Forest Of Habits";

    private static final EmailContext EMAIL_CONTEXT = EmailContext.builder()
            .emailLanguage(LANGUAGE)
            .from(ADDRESS_FROM)
            .attachment(null)
            .fromDisplayName(NAME_FROM)
            .subject(SUBJECT)
            .displayName(DISPLAY_NAME)
            .templateLocation("html/email.html")
            .email("")
            .context(null)
            .build();

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendSimpleEmail(String toAddress, String subject, String message) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(toAddress);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);
        simpleMailMessage.setFrom(ADDRESS_FROM);
        emailSender.send(simpleMailMessage);
    }

    public void sendMail(String toAddress) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            EMAIL_CONTEXT.setTo(toAddress);
            Context context = new Context();
            context.setVariables(EMAIL_CONTEXT.getContext());
            context.setVariable("imageResourceName", "/image/logo.png");

            mimeMessageHelper.addInline("image/logo.png", new ClassPathResource("image/logo.png"), "image/png");
            String emailContent = templateEngine.process(EMAIL_CONTEXT.getTemplateLocation(), context);


            mimeMessageHelper.setTo(EMAIL_CONTEXT.getTo());
            mimeMessageHelper.setSubject(EMAIL_CONTEXT.getSubject());
            mimeMessageHelper.setFrom(EMAIL_CONTEXT.getFrom());
            mimeMessageHelper.setText(emailContent, true);

            emailSender.send(message);
        } catch (Exception ignore) {}
    }
}