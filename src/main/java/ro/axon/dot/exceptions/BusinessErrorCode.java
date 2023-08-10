package ro.axon.dot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum BusinessErrorCode {

    EMPLOYEE_NOT_FOUND("EDOT0001400", "Employee not found", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_NOT_FOUND("EDOT0002400", "Leave request not found", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_REJECTED("EDOT0003400", "Leave request already rejected", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_PAST_DATE("EDOT0004400", "Leave request cannot be submitted for past dates.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_PRECEDING_VERSION("EDOT0005409", "Leave request version cannot precede version from database", HttpStatus.CONFLICT);

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
