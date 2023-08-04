package ro.axon.dot.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.service.EmployeeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmployeeApi {

  private final EmployeeService employeeService;

  @GetMapping(value = {"/employees/{name}", "/employees"})
  public ResponseEntity<EmployeeDetailsList> getEmployeesList(@PathVariable(required = false) String name){
    return ResponseEntity.ok(employeeService.getEmployeesDetails(name));
  }

}
