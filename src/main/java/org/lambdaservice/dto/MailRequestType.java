package org.lambdaservice.dto;

public enum MailRequestType {
    NOTIFICATION,
    PASSWORD_RESET;

    public static void validateType(String type) {
        try {
            MailRequestType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The request type does not match the application's requests");
        }
    }
}
