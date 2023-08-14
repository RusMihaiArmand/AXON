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
    REQUEST_HEADER_INVALID("EDOT0011400", "Request header invalid", HttpStatus.BAD_REQUEST),

    USERNAME_ALREADY_EXISTS("EDOT0012409", "Username already exists", HttpStatus.CONFLICT),
    EMPLOYEE_NOT_FOUND("EDOT0013404", "The employee with the given ID does not exist.", HttpStatus.NOT_FOUND),
    TEAM_NOT_FOUND("EDOT0014404", "Team not found", HttpStatus.NOT_FOUND),
    YEARLY_DAYS_OFF_NOT_SET("EDOT0015400", "The vacation days for this employee have not been set for this year.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_NOT_FOUND("EDOT0016400", "The leave request with the given ID does not exist.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_REJECTED("EDOT0017400", "Leave request already rejected.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_PAST_DATE("EDOT0018400", "Leave request cannot be submitted for past dates.", HttpStatus.BAD_REQUEST),
    LEAVE_REQUEST_PRECEDING_VERSION("EDOT0019409", "Leave request version cannot precede version from database.", HttpStatus.CONFLICT);

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
