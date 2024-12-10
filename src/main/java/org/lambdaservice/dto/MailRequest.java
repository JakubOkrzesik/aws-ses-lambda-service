package org.lambdaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailRequest {
    private String[] recipients;
    private String body;
    private String type;
    private String url;
}
