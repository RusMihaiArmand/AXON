package ro.axon.dot.api;


import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.ErrorDetail;

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

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail> handleException(MethodArgumentNotValidException exception){

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));

        errorDetail.setMessage(exception.getBindingResult().getAllErrors()
            .stream()
            .map(error -> Objects.requireNonNull(error.getDefaultMessage()))
                .collect(Collectors.joining(","))

            );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetail);
    }

}
