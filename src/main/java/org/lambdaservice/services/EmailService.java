package org.lambdaservice.services;


import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import lombok.RequiredArgsConstructor;
import org.lambdaservice.dto.MailRequest;
import org.lambdaservice.dto.MailRequestType;
import org.lambdaservice.dto.MailResponse;
import org.lambdaservice.dto.MailResponseStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final AmazonSimpleEmailService amazonSimpleEmailService;
    @Value("${MAIL_SENDER_ADDRESS}")
    private String source;

    Logger logger = LoggerFactory.getLogger(EmailService.class);

    public MailResponse send(MailRequest mailRequest) {
            try {
                MailRequestType.validateType(mailRequest.getType());

                SendEmailRequest request = new SendEmailRequest()
                        .withSource(source)
                        .withDestination(new Destination(mailRequest.getRecipients()))
                        .withMessage(generateMessage(mailRequest));

                amazonSimpleEmailService.sendEmail(request);

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

    private Message generateMessage(MailRequest mailRequest) {
        switch (MailRequestType.valueOf(mailRequest.getType())) {
            case PASSWORD_RESET -> {
                return new Message()
                        .withBody(new Body().withText(new Content("This is a password reset link: " + mailRequest.getPassword_reset_url())))
                        .withSubject(new Content("Password reset"));
            }
            case NOTIFICATION -> {
                return new Message()
                        .withBody(new Body().withText(new Content(mailRequest.getBody())))
                        .withSubject(new Content("Notification"));
                // will probably put sth like order number as the subject
            }
            default -> throw new IllegalArgumentException("The request type does not match the application's requests");
        }
    }

}
