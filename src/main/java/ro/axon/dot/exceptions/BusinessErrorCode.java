package ro.axon.dot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BusinessErrorCode {

    EMPLOYEE_NOT_FOUND("EDOT0001400", "The employee with the given ID does not exist.", HttpStatus.BAD_REQUEST),
    YEARLY_DAYS_OFF_NOT_SET("EDOT0002400", "The vacation days for this employee have not been set for this year.", HttpStatus.BAD_REQUEST),
    USERNAME_DUPLICATE("EDOT0001409", "An employee with this username already exists.", HttpStatus.CONFLICT),
    EMAIL_DUPLICATE("EDOT0002409", "An employee with this email already exists.", HttpStatus.CONFLICT);

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
