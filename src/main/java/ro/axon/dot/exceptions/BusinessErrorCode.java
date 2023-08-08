package ro.axon.dot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum BusinessErrorCode {

    EMPLOYEE_NOT_FOUND("EDOT0000404", "Employee not found", HttpStatus.NOT_FOUND),
    TOKEN_PARSING_FAILED("EDOT0001401", "Token failed to parse", HttpStatus.BAD_REQUEST),
    TOKEN_CANNOT_BE_SIGNED("EDOT0002401", "Token cannot be signed", HttpStatus.INTERNAL_SERVER_ERROR),
    SIGNER_CREATION_FAILED("EDOT0003401", "Token signer cannot be created", HttpStatus.INTERNAL_SERVER_ERROR),
    CLAIMSET_NOT_AVAILABLE("EDOT0004401", "Token claim set not available", HttpStatus.BAD_REQUEST),
    TOKEN_HAS_NO_USERNAME("EDOT0005401", "Token has no username", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID("EDOT0006401", "Token invalid", HttpStatus.BAD_REQUEST),
    TOKEN_CANNOT_BE_VERIFIED("EDOT0007401", "Token cannot be verified", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCHING("EDOT0008400", "Password does not match", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_NOT_FOUND("EDOT0009404", "Refresh token not found", HttpStatus.NOT_FOUND),


    EMPLOYEE_ALREADY_EXISTS("EDOT0009404", "Employee already exists", HttpStatus.NOT_FOUND),
    TEAM_NOT_FOUND("EDOT0009404", "Team not found", HttpStatus.NOT_FOUND);

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
