package ro.axon.dot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum BusinessErrorCode {

    EMPLOYEE_NOT_FOUND("EDOT0000404", "Employee not found", HttpStatus.NOT_FOUND),
    TOKEN_PARSING_FAILED("EDOT0000401", "Token failed to parse", HttpStatus.UNAUTHORIZED),
    TOKEN_CANNOT_BE_SIGNED("EDOT0001500", "Token cannot be signed", HttpStatus.INTERNAL_SERVER_ERROR),
    SIGNER_CREATION_FAILED("EDOT0002500", "Token signer cannot be created", HttpStatus.INTERNAL_SERVER_ERROR),
    CLAIMSET_NOT_AVAILABLE("EDOT0003500", "Token claim set not available", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_HAS_NO_USERNAME("EDOT0004401", "Token has no username", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("EDOT0005401", "Token invalid", HttpStatus.BAD_REQUEST),
    TOKEN_CANNOT_BE_VERIFIED("EDOT0006401", "Token cannot be verified", HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_MATCHING("EDOT0007401", "Password does not match", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND("EDOT0008404", "Refresh token not found", HttpStatus.NOT_FOUND),


    EMPLOYEE_ALREADY_EXISTS("EDOT0009404", "Employee already exists", HttpStatus.NOT_FOUND),
    TEAM_NOT_FOUND("EDOT0010404", "Team not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("EDOT0011404", "User not found", HttpStatus.NOT_FOUND),
    AUDIENCE_DOES_NOT_MATCH("EDOT0012401", "Audience doesn't match", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED("EDOT0013401", "Token is revoked", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("EDOT0014401", "Token is expired", HttpStatus.UNAUTHORIZED),
    REQUEST_HEADER_INVALID("EDOT0015400", "Request header invalid", HttpStatus.BAD_REQUEST),
    CLAIMSET_INVALID("EDOT0016400", "Token claim set is invalid", HttpStatus.BAD_REQUEST),
    FAILED_TO_READ_KEYS("EDOT0017500", "Failed to read jwk keys", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
