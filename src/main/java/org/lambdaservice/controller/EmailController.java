package org.lambdaservice.controller;

import lombok.RequiredArgsConstructor;
import org.lambdaservice.dto.MailRequest;
import org.lambdaservice.dto.MailRequestType;
import org.lambdaservice.dto.MailResponse;
import org.lambdaservice.services.EmailService;
import org.lambdaservice.services.ResponseHandlerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final ResponseHandlerService responseService;

    @PostMapping(value = "/send", produces = "application/json")
    public ResponseEntity<Object> send(@RequestBody List<MailRequest> requestList) {
        try {
            List<MailResponse> responses = requestList.stream()
                    .peek(this::validateRequestFields) // Validate the type
                    .map(emailService::send) // Send the email if validation passes
                    .toList(); // Compile results to a list

            return responseService.generateResponse("Returning emails requests status", HttpStatus.CREATED, responses);
        } catch (IllegalArgumentException e) {
            return responseService.generateResponse("Invalid request", HttpStatus.BAD_REQUEST, e.getMessage());
        }
        catch (Exception e) {
            return responseService.generateResponse("Error while processing the request", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // Temporary solution real deal validation should be implemented in the future
    private void validateRequestFields(MailRequest mailRequest) {
        if (mailRequest.getRecipients()==null || mailRequest.getRecipients().isEmpty() || mailRequest.getType()==null) {
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
                case PASSWORD_RESET -> {
                    if (mailRequest.getPassword_reset_url() == null || mailRequest.getPassword_reset_url().isEmpty()) {
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
