package ro.axon.dot.exceptions;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private final BusinessExceptionElement error;

    public BusinessException(Throwable cause, BusinessExceptionElement error) {
        super(cause);
        this.error = error;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class BusinessExceptionElement {

        private final BusinessErrorCode errorDescription;
        private final Map<String, Object> contextVariables;
    }
}
