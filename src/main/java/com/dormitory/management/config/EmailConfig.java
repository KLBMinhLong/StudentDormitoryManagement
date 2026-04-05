package com.dormitory.management.config;

import java.util.Properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host:smtp-relay.brevo.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private String startTlsEnable;

    @Value("${spring.mail.properties.mail.smtp.starttls.required:true}")
    private String startTlsRequired;

    @Value("${spring.mail.properties.mail.smtp.ssl.protocols:TLSv1.2}")
    private String sslProtocols;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable:false}")
    private String sslEnable;

    @Value("${spring.mail.properties.mail.smtp.connectiontimeout:20000}")
    private String smtpConnectionTimeout;

    @Value("${spring.mail.properties.mail.smtp.timeout:20000}")
    private String smtpTimeout;

    @Value("${spring.mail.properties.mail.smtp.writetimeout:20000}")
    private String smtpWriteTimeout;

    @Bean
    @ConditionalOnProperty(name = "spring.mail.host")
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", startTlsEnable);
        props.put("mail.smtp.starttls.required", startTlsRequired);
        props.put("mail.smtp.ssl.enable", sslEnable);
        props.put("mail.smtp.ssl.protocols", sslProtocols);
        props.put("mail.smtp.connectiontimeout", smtpConnectionTimeout);
        props.put("mail.smtp.timeout", smtpTimeout);
        props.put("mail.smtp.writetimeout", smtpWriteTimeout);

        return mailSender;
    }
}
