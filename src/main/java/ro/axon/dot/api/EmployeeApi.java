package ro.axon.dot.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.RemainingDaysOff;
import ro.axon.dot.service.EmployeeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmployeeApi {

  private final EmployeeService employeeService;

  @GetMapping(value = {"/employees/{name}", "/employees"})
  public ResponseEntity<EmployeeDetailsList> getEmployeesList(@RequestParam(required = false) String name){

    EmployeeDetailsList employeesList = employeeService.getEmployeesDetails(name);

    return ResponseEntity.ok(employeesList);
  }

  @GetMapping(value = {"/employees/{employeeId}/remaining-days-off"})
  public ResponseEntity<RemainingDaysOff> getEmployeeRemainingDaysOff(@PathVariable String employeeId){

    RemainingDaysOff remainingDaysOff = employeeService.getEmployeeRemainingDaysOff(employeeId);

    return ResponseEntity.ok(remainingDaysOff);
  }
}
