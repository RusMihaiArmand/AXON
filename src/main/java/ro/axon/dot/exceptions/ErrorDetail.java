package ro.axon.dot.exceptions;

import lombok.Data;

import java.util.Map;

@Data
public class ErrorDetail {
    private String message;
    private String errorCode;
    private Map<String, Object> contextVariables;
}
