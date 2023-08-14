package ro.axon.dot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BusinessErrorCode {

    EMPLOYEE_NOT_FOUND("EDOT0001400", "The employee with the given ID does not exist.", HttpStatus.BAD_REQUEST),
    YEARLY_DAYS_OFF_NOT_SET("EDOT0002400", "The vacation days for this employee have not been set for this year.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_NOT_FOUND("EDOT0003400", "The leave request with the given ID does not exist.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_REJECTED("EDOT0004400", "Leave request already rejected.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_PAST_DATE("EDOT0005400", "Leave request cannot be submitted for past dates.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_PRECEDING_VERSION("EDOT0006409", "Leave request version cannot precede version from database.", HttpStatus.CONFLICT);

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
