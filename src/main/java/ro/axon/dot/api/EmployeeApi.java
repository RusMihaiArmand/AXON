package ro.axon.dot.api;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.service.EmployeeService;
import ro.axon.dot.service.LeaveRequestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmployeeApi {

  private final EmployeeService employeeService;
  private final LeaveRequestService leaveRequestService;

  @GetMapping(value = "/employees")
  public ResponseEntity<EmployeeDetailsList> getEmployeesList(@RequestParam(required = false) String name){

    EmployeeDetailsList employeesList = employeeService.getEmployeesDetails(name);

    return ResponseEntity.ok(employeesList);
  }

  @PatchMapping(value = "/employees/{employeeId}/inactivate")
  public ResponseEntity<Void> inactivateEmployee(@PathVariable String employeeId){
    employeeService.inactivateEmployee(employeeId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("employees/{employeeId}/requests/{requestId}")
  public ResponseEntity<Void> editLeaveRequest(@PathVariable String employeeId,
      @PathVariable Long requestId,
      @Valid @RequestBody EditLeaveRequestDetails leaveRequestDetails){

    leaveRequestService.editLeaveRequest(employeeId, requestId, leaveRequestDetails);

    return ResponseEntity.noContent().build();
  }
}
