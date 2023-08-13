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
    CLAIMSET_INVALID("EDOT0006400", "Token claim set is invalid", HttpStatus.BAD_REQUEST),
    TOKEN_CANNOT_BE_VERIFIED("EDOT0007401", "Token cannot be verified", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND("EDOT0008404", "Refresh token not found", HttpStatus.NOT_FOUND),
    TOKEN_INVALID("EDOT0009401", "Token invalid", HttpStatus.BAD_REQUEST),
    TOKEN_REVOKED("EDOT0010401", "Token is revoked", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("EDOT0011401", "Token is expired", HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_MATCHING("EDOT0012401", "Password does not match", HttpStatus.UNAUTHORIZED),
    AUDIENCE_DOES_NOT_MATCH("EDOT0013401", "Audience doesn't match", HttpStatus.UNAUTHORIZED),
    REQUEST_HEADER_INVALID("EDOT0014400", "Request header invalid", HttpStatus.BAD_REQUEST),

    USERNAME_ALREADY_EXISTS("EDOT0015409", "Username already exists", HttpStatus.CONFLICT),
    EMPLOYEE_NOT_FOUND("EDOT0016404", "Employee not found", HttpStatus.NOT_FOUND),
    TEAM_NOT_FOUND("EDOT0017404", "Team not found", HttpStatus.NOT_FOUND),
    TOKEN_NOT_VALID_YET("EDOT0018401", "Token not valid yet", HttpStatus.UNAUTHORIZED);

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
