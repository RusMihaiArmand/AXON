package ro.axon.dot.api;

import java.time.LocalDate;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.model.*;
import ro.axon.dot.service.EmployeeService;
import ro.axon.dot.service.LeaveRequestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmployeeApi {

  private final EmployeeService employeeService;
  private final LeaveRequestService leaveRequestService;

  @GetMapping(value = "/employees")
  public ResponseEntity<EmployeeDetailsList> getEmployeesList(
      @RequestParam(required = false) String name) {

    EmployeeDetailsList employeesList = employeeService.getEmployeesDetails(name);

    return ResponseEntity.ok(employeesList);
  }

  @PatchMapping(value = "/employees/{employeeId}/inactivate")
  public ResponseEntity<Void> inactivateEmployee(@PathVariable String employeeId) {
    employeeService.inactivateEmployee(employeeId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("employees/{employeeId}/requests/{requestId}")
  public ResponseEntity<Void> editLeaveRequest(@PathVariable String employeeId,
      @PathVariable Long requestId,
      @Valid @RequestBody EditLeaveRequestDetails leaveRequestDetails) {

    employeeService.editLeaveRequest(employeeId, requestId, leaveRequestDetails);

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("employees/{idEmployee}/requests/{idRequest}")
  public ResponseEntity<Void> handleLeaveRequest(@PathVariable(name = "idEmployee") Long idEmployee,
      @PathVariable(name = "idRequest") Long idRequest, @RequestBody LeaveRequestReview review) {
    employeeService.updateLeaveRequestStatus(idEmployee, idRequest, review);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("employees/{employeeId}/requests/{requestId}")
  public ResponseEntity<Void> deleteLeaveRequest(@PathVariable String employeeId,
      @PathVariable Long requestId) {

    employeeService.deleteLeaveRequest(employeeId, requestId);

    return ResponseEntity.noContent().build();
  }


  @GetMapping(value = {"/employees/{employeeId}/remaining-days-off"})
  public ResponseEntity<RemainingDaysOff> getEmployeeRemainingDaysOff(
      @PathVariable String employeeId) {

    RemainingDaysOff remainingDaysOff = employeeService.getEmployeeRemainingDaysOff(employeeId);

    return ResponseEntity.ok(remainingDaysOff);
  }

  @PostMapping("/employees/{employeeId}/requests")
  public ResponseEntity<Void> addLeaveRequest(@PathVariable String employeeId,
      @Valid @RequestBody CreateLeaveRequestDetails createLeaveRequestDetails) {

    employeeService.createLeaveRequest(employeeId, createLeaveRequestDetails);
    return ResponseEntity.status(HttpStatus.CREATED).build();

  }

  @GetMapping(value = "/employee/validation")
  public ResponseEntity<Void> checkEmployeeUniqueCredentials(
      @RequestParam(name = "username", required = false) String username,
      @RequestParam(name = "email", required = false) String email) {

    if (employeeService.checkEmployeeUniqueCredentials(username, email)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.badRequest().build();
  }

  @GetMapping(value = "/employees/{employeeId}/requests")
  public ResponseEntity<LeaveRequestDetailsList> getLeaveRequests(
      @PathVariable(name = "employeeId") String idEmployee,
      @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
    return ResponseEntity.ok(employeeService.getLeaveRequests(idEmployee, startDate, endDate));
  }

  @PostMapping("/employees/days-off")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void modifyEmployeesDaysOff(@Valid @RequestBody VacationDaysModifyDetails vacationDaysModifyDetails)
  {
    employeeService.changeVacationDays(vacationDaysModifyDetails);
  }

  @PatchMapping("/employees/{employeeId}")
  public ResponseEntity<Void> updateEmployeeDetails(@PathVariable String employeeId,
                                                    @Valid @RequestBody EmployeeUpdateRequest employeeUpdateRequest) {

    employeeService.updateEmployeeDetails(employeeId, employeeUpdateRequest);

    return ResponseEntity.noContent().build();
  }
}
