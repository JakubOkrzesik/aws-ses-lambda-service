package org.lambdaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class MailResponse {
    private String recipients;
    private String type;
    private MailResponseStatusType status;
    private String msg;
}
