package ro.axon.dot.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.model.EmployeeDto;
import ro.axon.dot.service.UpdateEmployeeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UpdateEmployeeApi {

  private final UpdateEmployeeService updateEmployeeService;

  @PatchMapping("/employees/{employeeId}")
  public ResponseEntity<Void> updateEmployeeDetails(@PathVariable String employeeId,
      @RequestBody EmployeeDto employeeDto) {

        try {
          updateEmployeeService.updateEmployeeDetails(employeeId, employeeDto);
          return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
          if (e.getError().getErrorDescription().equals(BusinessErrorCode.CONFLICT)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
          } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
          }
        }
  }
}
