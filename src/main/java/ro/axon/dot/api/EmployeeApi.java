package ro.axon.dot.api;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestReview;
import ro.axon.dot.model.RegisterRequest;
import ro.axon.dot.model.RemainingDaysOff;
import ro.axon.dot.model.UserDetailsResponse;
import ro.axon.dot.service.EmployeeService;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EmployeeApi {

  private final EmployeeService employeeService;

  @PostMapping(value = "/employees/register")
  public ResponseEntity<?> register(@RequestHeader(name="Authorization") String token, @RequestBody @Valid RegisterRequest request) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", token);

    ResponseEntity<UserDetailsResponse> response = new RestTemplate().exchange(
        "http://localhost:8081/core/api/v1/user",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        UserDetailsResponse.class
    );

    UserDetailsResponse userDetails = response.getBody();
    return ResponseEntity.ok(employeeService.createEmployee(request, userDetails));
  }

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
                                                 @PathVariable Long requestId){

    employeeService.deleteLeaveRequest(employeeId, requestId);

    return ResponseEntity.noContent().build();
  }


  @GetMapping(value = {"/employees/{employeeId}/remaining-days-off"})
  public ResponseEntity<RemainingDaysOff> getEmployeeRemainingDaysOff(@PathVariable String employeeId){

    RemainingDaysOff remainingDaysOff = employeeService.getEmployeeRemainingDaysOff(employeeId);

    return ResponseEntity.ok(remainingDaysOff);
  }

  @GetMapping(value = "/employee/validation")
  public ResponseEntity<Void> checkEmployeeUniqueCredentials(@RequestParam(name = "username", required = false) String username,
                                                          @RequestParam(name = "email", required = false) String email) {

    if (employeeService.checkEmployeeUniqueCredentials(username, email))
      return ResponseEntity.ok().build();
    return ResponseEntity.badRequest().build();
  }

  @GetMapping(value = "/employees/{employeeId}/requests")
  public ResponseEntity<LeaveRequestDetailsList> getLeaveRequests(
        @PathVariable(name = "employeeId") String idEmployee,
        @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
            return ResponseEntity.ok(employeeService.getLeaveRequests(idEmployee, startDate, endDate));
  }
}
