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
    LEAVE_REQUEST_DELETE_APPROVED_PAST_DATE("EDOT0006400", "Cannot delete approved leave requests from past months.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_PRECEDING_VERSION("EDOT0007409", "Leave request version cannot precede version from database.", HttpStatus.CONFLICT),
    USERNAME_DUPLICATE("EDOT0008409", "An employee with this username already exists.", HttpStatus.CONFLICT),
    EMAIL_DUPLICATE("EDOT0009409", "An employee with this email already exists.", HttpStatus.CONFLICT),
    LEAVE_RQST_DIFF_YEARS("ED0T0010400", "Leave request has different years", HttpStatus.BAD_REQUEST),
    LEAVE_RQST_INVALID_PERIOD("ED0T0011400", "Invalid period for leave request", HttpStatus.BAD_REQUEST),
    LEAVE_RQST_INVALID_NUMBER_DAYS("ED0T0012400", "Leave request has too many days", HttpStatus.BAD_REQUEST),
    LEAVE_RQST_INVALID_MONTH("ED0T0013400", "Invalid month for leave request", HttpStatus.BAD_REQUEST),
    NEGATIVE_DAYS_OFF("EDOT0001500", "Number of days off became negative.", HttpStatus.BAD_REQUEST),
    EMPLOYEE_VERSION_CONFLICT("EDOT0010409", "Conflict on employee version.", HttpStatus.CONFLICT),
    TEAM_NOT_FOUND("EDOT0011400", "The given team ID does not exist", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
