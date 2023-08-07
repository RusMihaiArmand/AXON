package ro.axon.dot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum BusinessErrorCode {

    EMPLOYEE_NOT_FOUND("EDOT0001400", "Employee not found", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
