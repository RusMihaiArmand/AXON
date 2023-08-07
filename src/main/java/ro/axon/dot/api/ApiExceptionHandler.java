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
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorCode(exception.getError().getErrorDescription().getErrorCode());
        errorDetail.setMessage(exception.getError().getErrorDescription().getDevMsg());
        errorDetail.setContextVariables(exception.getError().getContextVariables());
        return ResponseEntity.status(exception.getError().getErrorDescription().getStatus())
                .body(errorDetail);
    }
}
