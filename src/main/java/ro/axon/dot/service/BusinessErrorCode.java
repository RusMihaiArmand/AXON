package ro.axon.dot.service;

import org.springframework.http.HttpStatus;

public enum BusinessErrorCode {;

    private final String errorCode;
    private final String devMsg;
    private final HttpStatus status;

    BusinessErrorCode(String errorCode, String devMsg, HttpStatus status) {
        this.errorCode = errorCode;
        this.devMsg = devMsg;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDevMsg() {
        return devMsg;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
