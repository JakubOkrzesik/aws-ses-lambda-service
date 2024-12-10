package org.lambdaservice.services;


import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.*;
import lombok.RequiredArgsConstructor;
import org.lambdaservice.dto.MailRequest;
import org.lambdaservice.dto.MailRequestType;
import org.lambdaservice.dto.MailResponse;
import org.lambdaservice.dto.MailResponseStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    private MimeMessage generateMessage(MailRequest mailRequest) throws MessagingException, IOException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        mimeMessage.setFrom(source);
        Address[] recipients = getAddresses(mailRequest);
        mimeMessage.setRecipients(Message.RecipientType.TO, recipients);
        MimeMultipart multipart = new MimeMultipart("related");
        BodyPart bodyPart = new MimeBodyPart();
        String htmlText;


        switch (MailRequestType.valueOf(mailRequest.getType())) {
            case PASSWORD_RESET -> {
                mimeMessage.setSubject("This is a password reset email");
                mimeMessage.setText(mailRequest.getUrl());

                htmlText = loadHtmlTemplate("src/main/resources/templates/account-activation-template.html");
                bodyPart.setContent(htmlText, "text/html");

                mimeMessage.setContent(multipart);
                return mimeMessage;
            }
            case NOTIFICATION -> {
                mimeMessage.setSubject("This is a notification");
                mimeMessage.setText(mailRequest.getBody());

                htmlText = loadHtmlTemplate("src/main/resources/templates/notification-template.html");
                bodyPart.setContent(htmlText, "text/html");

                return mimeMessage;
                // will probably put sth like order number as the subject
            }
            case ACCOUNT_ACTIVATION -> {
                mimeMessage.setSubject("This is a account activation email");
                mimeMessage.setText(mailRequest.getUrl());

                htmlText = loadHtmlTemplate("src/main/resources/templates/account-activation-template.html");
                bodyPart.setContent(htmlText, "text/html");

                return mimeMessage;
            }
            default -> throw new IllegalArgumentException("The request type does not match the application's requests");
        }
    }

    private String loadHtmlTemplate(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
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

    // Temporary solution real deal validation should be implemented in the future
    public void validateRequestFields(MailRequest mailRequest) {
        if (mailRequest.getRecipients()==null || mailRequest.getRecipients().length==0 || mailRequest.getType()==null) {
            throw new IllegalArgumentException("Invalid request params - check your request");
        }

        try {
            // Validate the request type
            MailRequestType type = MailRequestType.valueOf(mailRequest.getType());

            switch (type) {
                case NOTIFICATION -> {
                    if (mailRequest.getBody() == null || mailRequest.getBody().isEmpty()) {
                        throw new IllegalArgumentException("The request body must not be null or empty for type 'NOTIFICATION'");
                    }
                }
                case PASSWORD_RESET, ACCOUNT_ACTIVATION -> {
                    if (mailRequest.getUrl() == null || mailRequest.getUrl().isEmpty()) {
                        throw new IllegalArgumentException("The password reset URL must not be null or empty for type 'PASSWORD_RESET'");
                    }
                }
                default -> throw new IllegalArgumentException("The request type '" + mailRequest.getType() + "' does not match the application's requests");
            }
        } catch (IllegalArgumentException e) {
            // Handle invalid request type or missing fields
            throw new IllegalArgumentException("The request type '" + mailRequest.getType() + "' is invalid or missing required fields", e);
        }
    }

}
