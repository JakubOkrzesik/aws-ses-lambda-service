package org.lambdaservice.services;


import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.lambdaservice.dto.MailRequest;
import org.lambdaservice.dto.MailRequestType;
import org.lambdaservice.dto.MailResponse;
import org.lambdaservice.dto.MailResponseStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${MAIL_SENDER_ADDRESS}")
    private String source;
    @Autowired
    private final JavaMailSender mailSender;

    Logger logger = LoggerFactory.getLogger(EmailService.class);

    public MailResponse send(MailRequest mailRequest) {
            try {
                MailRequestType.validateType(mailRequest.getType());


                mailSender.send(generateMessage(mailRequest));

                return new MailResponse(
                        String.join(",", mailRequest.getRecipients()),
                        mailRequest.getType(),
                        MailResponseStatusType.SUCCESS,
                        "Email sent successfully"
                );
            } catch (Exception e) {
                System.out.println(e);
                logger.error("Failed to send email for: " + mailRequest, e);

                return new MailResponse(
                        String.join(",", mailRequest.getRecipients()),
                        mailRequest.getType(),
                        MailResponseStatusType.FAILURE,
                        "Error occurred while sending email: " + e.getMessage()
                );
            }
    }

    private MimeMessage generateMessage(MailRequest mailRequest) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        mimeMessage.setFrom(source);
        Address[] recipients = getAddresses(mailRequest);
        mimeMessage.setRecipients(Message.RecipientType.TO, recipients);

        switch (MailRequestType.valueOf(mailRequest.getType())) {
            case PASSWORD_RESET -> {
                mimeMessage.setSubject("This is a password reset email");
                mimeMessage.setText(mailRequest.getPassword_reset_url());
                return mimeMessage;
            }
            case NOTIFICATION -> {
                mimeMessage.setSubject("This is a notification");
                mimeMessage.setText(mailRequest.getBody());
                return mimeMessage;
                // will probably put sth like order number as the subject
            }
            default -> throw new IllegalArgumentException("The request type does not match the application's requests");
        }
    }

    private static Address[] getAddresses(MailRequest mailRequest) {
        return Arrays.stream(mailRequest.getRecipients())
                .map(recipient -> {
                    try {
                        return new InternetAddress(recipient); // Create an InternetAddress object for each recipient
                    } catch (AddressException e) {
                        throw new IllegalArgumentException("Invalid email address: " + recipient, e); // Handle invalid address
                    }
                })
                .toArray(Address[]::new);
    }

}
