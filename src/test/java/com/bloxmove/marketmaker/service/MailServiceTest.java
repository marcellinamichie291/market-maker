package com.bloxmove.marketmaker.service;

import com.bloxmove.marketmaker.config.EmailProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.verify;

@SpringBootTest
public class MailServiceTest {

    @Autowired
    private MailService mailService;
    @Autowired
    private EmailProperties emailProperties;
    @MockBean
    private JavaMailSender mailSender;

    @Test
    void shouldSendMail() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(emailProperties.getAddressee());
        simpleMailMessage.setFrom(emailProperties.getAddressee());
        simpleMailMessage.setSubject("subject");
        simpleMailMessage.setText("text");

        mailService.send("subject", "text");

        verify(mailSender).send(simpleMailMessage);
    }

}