package org.lambdaservice.testFiles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lambdaservice.dto.MailResponse;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BodyTestClass {
    private List<MailResponse> data;
    private String message;
    private Integer status;
}
