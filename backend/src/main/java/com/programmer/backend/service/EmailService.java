package com.programmer.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false) 
    private JavaMailSender mailSender;

    public void sendSimpleMessage(String to, String subject, String text) {
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
              
                message.setFrom("noreply@programmer.com");
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                mailSender.send(message);
            } catch (Exception e) {
                System.err.println("Error enviando email: " + e.getMessage());
            }
        } else {
            System.err.println("JavaMailSender no está configurado. Simulación de envío:");
            System.out.println("---------------------------------------------------------");
            System.out.println("Para: " + to);
            System.out.println("Asunto: " + subject);
            System.out.println("Texto: \n" + text);
            System.out.println("---------------------------------------------------------");
        }
    }
}
