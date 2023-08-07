package ro.axon.dot.service;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private final BusinessExceptionElement error;

    public BusinessException(Throwable cause, BusinessExceptionElement error) {
        super(cause);
        this.error = error;
    }
}
