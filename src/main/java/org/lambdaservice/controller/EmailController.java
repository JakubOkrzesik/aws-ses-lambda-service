package org.lambdaservice.controller;

import lombok.RequiredArgsConstructor;
import org.lambdaservice.dto.MailRequest;
import org.lambdaservice.dto.MailRequestType;
import org.lambdaservice.dto.MailResponse;
import org.lambdaservice.dto.MailServiceException;
import org.lambdaservice.services.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping(value = "/send", produces = "application/json")
    public ResponseEntity<Object> send(@RequestBody List<MailRequest> requestList) {
        try {
            List<MailResponse> responses = requestList.stream()
                    .peek(emailService::validateRequestFields) // Validate the type
                    .map(emailService::send) // Send the email if validation passes
                    .toList(); // Compile results to a list

            return generateResponse("Returning emails requests status", HttpStatus.CREATED, responses);
        } catch (IllegalArgumentException e) {
            return generateResponse("Invalid request", HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return generateResponse("Error while processing the request", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }



    private ResponseEntity<Object> generateResponse(String message, HttpStatus status, Object responseObj) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status.value());
        map.put("data", responseObj);

        return ResponseEntity
                .status(status)
                .body(map);
    }

}
