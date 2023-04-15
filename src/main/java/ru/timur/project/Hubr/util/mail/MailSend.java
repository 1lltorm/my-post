package ru.timur.project.Hubr.util.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import ru.timur.project.Hubr.models.User;

@Component
public class MailSend {
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String username;

    @Autowired
    public MailSend(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void send(String emailTo, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(username);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        javaMailSender.send(mailMessage);
    }

    public void activateUserMessage(User user) {
        String message = String.format(
                "Hello, %s! \n" +
                        "Welcome to Hubr. Please visit next link: http://localhost:8080/auth/activate/%s",
                user.getUsername(),
                user.getActivationCode()
        );
        send(user.getEmail(), "Activation new account", message);
    }

    public void activateNewEmailMessage(User user) {
        String message = String.format(
                "Hello again, %s! \n" +
                        "Welcome to Hubr. To confirm your new email, please follow the link below: \n" +
                        "http://localhost:8080/auth/activate/new-email/%s/%s",
                user.getUsername(),
                user.getActivationCode(),
                user.getId()
        );

        send(user.getSecondEmail(), "Activation new email", message);
    }

}
