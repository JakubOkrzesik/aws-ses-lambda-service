package org.lambdaservice;


import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.lambdaservice.dto.MailRequest;
import org.lambdaservice.dto.MailResponse;
import org.lambdaservice.dto.MailResponseStatusType;
import org.lambdaservice.testFiles.BodyTestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StreamLambdaHandlerTest {

    private static StreamLambdaHandler handler;
    private static Context lambdaContext;
    Logger logger = LoggerFactory.getLogger(StreamLambdaHandlerTest.class);

    @BeforeAll
    public static void setUp() {
        handler = new StreamLambdaHandler();
        lambdaContext = new MockLambdaContext();
    }

    @Test
    public void sendEmail_TwoValidRequests_TwoSuccess() {
        File json = new File("src/test/java/org/lambdaservice/testFiles/test3.json");

        JsonNode parsedJson = readJsonFromFile(json);

        InputStream requestStream = new AwsProxyRequestBuilder("/email/send", HttpMethod.POST)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .body(parsedJson)
                .buildStream();
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

        handle(requestStream, responseStream);

        AwsProxyResponse response = readResponse(responseStream);
        assertNotNull(response);

        String responseBody = response.getBody();
        BodyTestClass body = readResponseBody(responseBody);
        assertNotNull(body);
        System.out.println(body);

        assertEquals(HttpStatus.valueOf(response.getStatusCode()), HttpStatus.CREATED);

        List<MailResponse> mailResponseList = body.getData();

        assertEquals(MailResponseStatusType.SUCCESS, mailResponseList.get(0).getStatus());
        assertEquals(MailResponseStatusType.SUCCESS, mailResponseList.get(1).getStatus());
    }

    @Test
    public void sendEmail_NullStatus_BadRequest() {
        File json = new File("src/test/java/org/lambdaservice/testFiles/test.json");

        JsonNode parsedJson = readJsonFromFile(json);

        InputStream requestStream = new AwsProxyRequestBuilder("/email/send", HttpMethod.POST)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .body(parsedJson)
                .buildStream();
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

        handle(requestStream, responseStream);

        AwsProxyResponse response = readResponse(responseStream);
        assertNotNull(response);
        logger.error(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(response.getStatusCode()));
    }

    @Test
    public void sendEmail_NullBodyOnNotificationRequest_BadRequest() {
        File json = new File("src/test/java/org/lambdaservice/testFiles/test1.json");

        JsonNode parsedJson = readJsonFromFile(json);

        InputStream requestStream = new AwsProxyRequestBuilder("/email/send", HttpMethod.POST)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .body(parsedJson)
                .buildStream();
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();

        handle(requestStream, responseStream);

        AwsProxyResponse response = readResponse(responseStream);
        assertNotNull(response);
        logger.error(response.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, HttpStatus.valueOf(response.getStatusCode()));
    }

    private void handle(InputStream is, ByteArrayOutputStream os) {
        try {
            handler.handleRequest(is, os, lambdaContext);
        } catch (IOException e) {
            logger.error(e.getMessage());
            fail(e.getMessage());
        }
    }

    private JsonNode readJsonFromFile(File jsonFile) {
        try {
            return LambdaContainerHandler.getObjectMapper().readTree(jsonFile);
        } catch (IOException e) {
            logger.error(e.getMessage());
            fail("Error while parsing response: " + e.getMessage());
        }
        return null;
    }

    private AwsProxyResponse readResponse(ByteArrayOutputStream responseStream) {
        try {
            return LambdaContainerHandler.getObjectMapper().readValue(responseStream.toByteArray(), AwsProxyResponse.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            fail("Error while parsing response: " + e.getMessage());
        }
        return null;
    }

    private BodyTestClass readResponseBody(String responseBody) {
        try {
            return LambdaContainerHandler.getObjectMapper().readValue(responseBody, BodyTestClass.class);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            fail("Error while parsing response: " + e.getMessage());
        }
        return null;
    }
}
