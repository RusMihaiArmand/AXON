package ro.axon.dot.api;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ro.axon.dot.service.BusinessException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDetail> handleBusinessException(BusinessException exception) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorCode("EDOT0001400");
        errorDetail.setMessage(exception.getMessage());
        errorDetail.setContextVariables(new HashMap<>());
        return ResponseEntity.status(status)
                .body(errorDetail);
    }
}
