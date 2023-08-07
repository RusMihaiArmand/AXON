package ro.axon.dot.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum BusinessErrorCode {;

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;
}
