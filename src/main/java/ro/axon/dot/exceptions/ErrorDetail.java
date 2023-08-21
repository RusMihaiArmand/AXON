package ro.axon.dot.exceptions;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ErrorDetail {
    private String message;
    private String errorCode;
    private Map<String, Object> contextVariables;
}
