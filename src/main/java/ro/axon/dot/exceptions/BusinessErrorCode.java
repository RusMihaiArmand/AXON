package ro.axon.dot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BusinessErrorCode {

    FAILED_TO_READ_KEYS("EDOT0000500", "Failed to read jwk keys", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_PARSING_FAILED("EDOT0001401", "Token failed to parse", HttpStatus.UNAUTHORIZED),
    TOKEN_CANNOT_BE_SIGNED("EDOT0002500", "Token cannot be signed", HttpStatus.INTERNAL_SERVER_ERROR),
    SIGNER_CREATION_FAILED("EDOT0003500", "Token signer cannot be created", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_HAS_NO_USERNAME("EDOT0004401", "Token has no username", HttpStatus.UNAUTHORIZED),
    CLAIMSET_NOT_AVAILABLE("EDOT0005500", "Token claim set not available", HttpStatus.INTERNAL_SERVER_ERROR),
    REFRESH_TOKEN_NOT_FOUND("EDOT0006404", "Refresh token not found", HttpStatus.NOT_FOUND),
    TOKEN_REVOKED("EDOT0007401", "Token is revoked", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("EDOT0008401", "Token is expired", HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_MATCHING("EDOT0009401", "Password does not match", HttpStatus.UNAUTHORIZED),
    AUDIENCE_DOES_NOT_MATCH("EDOT0010401", "Audience doesn't match", HttpStatus.UNAUTHORIZED),
    EMPLOYEE_NOT_FOUND("EDOT0011404", "The employee with the given ID does not exist.", HttpStatus.NOT_FOUND),
    YEARLY_DAYS_OFF_NOT_SET("EDOT0012400", "The vacation days for this employee have not been set for this year.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_NOT_FOUND("EDOT0013400", "The leave request with the given ID does not exist.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_UPDATE_ALREADY_REJECTED("EDOT0014400", "Cannot update already rejected leave request.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_DELETE_ALREADY_REJECTED("EDOT0015400", "Cannot delete already rejected leave request.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_UPDATE_IN_PAST("EDOT0016400", "Cannot update leave request with date in the past.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_DELETE_APPROVED_PAST_DATE("EDOT0017400", "Cannot delete approved leave requests from past months.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_VERSION_CONFLICT("EDOT0018409", "Conflict on leave request version.", HttpStatus.CONFLICT),
    USERNAME_DUPLICATE("EDOT0019409", "An employee with this username already exists.", HttpStatus.CONFLICT),
    EMAIL_DUPLICATE("EDOT0020409", "An employee with this email already exists.", HttpStatus.CONFLICT),
    LEAVE_RQST_DIFF_YEARS("ED0T0021400", "Cannot create leave request with dates in different years.", HttpStatus.BAD_REQUEST),
    LEAVE_RQST_INVALID_PERIOD("ED0T0022400", "Invalid period for leave request", HttpStatus.BAD_REQUEST),
    LEAVE_RQST_INVALID_NUMBER_DAYS("ED0T0023400", "The number of days related to the leave request exceeds the number of days available.", HttpStatus.BAD_REQUEST),
    LEAVE_RQST_INVALID_MONTH("ED0T0024400", "Invalid month for leave request", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_NOT_PENDING("EDOT0025400", "Leave request must be pending in order to APPROVE or REJECT it.", HttpStatus.BAD_REQUEST),
    NEGATIVE_DAYS_OFF("EDOT0026500", "Number of days off became negative.", HttpStatus.BAD_REQUEST),
    TEAM_NOT_FOUND("EDOT0027404", "Team not found", HttpStatus.NOT_FOUND),
    NO_JWT_AUTH_FOUND("EDOT0028401", "No JWT Auth found in Security Context!", HttpStatus.UNAUTHORIZED),
    EMPLOYEE_VERSION_CONFLICT("EDOT0029409", "Conflict on employee version.", HttpStatus.CONFLICT),
    EMPLOYEE_DETAILS_VALIDATION_INVALID_REQUEST("EDOT0030400", "The username and the email are not provided in the query params.", HttpStatus.BAD_REQUEST),
    INVALID_DATE_PROVIDED("EDOT0031400", "Invalid date provided.", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
