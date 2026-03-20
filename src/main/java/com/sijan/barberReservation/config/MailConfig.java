package com.sijan.barberReservation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean(name = "gmailMailSender")
    public JavaMailSender gmailMailSender(
            @Value("${mail.gmail.host}") String host,
            @Value("${mail.gmail.port}") int port,
            @Value("${mail.gmail.username}") String username,
            @Value("${mail.gmail.password}") String password
    ) {
        return buildMailSender(host, port, username, password);
    }

    private JavaMailSender buildMailSender(String host, int port, String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", host); // Recommended for Gmail

        return mailSender;
    }
}
