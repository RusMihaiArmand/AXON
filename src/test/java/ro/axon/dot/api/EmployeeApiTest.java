package ro.axon.dot.api;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.axon.dot.EmployeeTestAttributes.CRT_TMS;
import static ro.axon.dot.EmployeeTestAttributes.CRT_USR;
import static ro.axon.dot.EmployeeTestAttributes.EMAIL;
import static ro.axon.dot.EmployeeTestAttributes.FIRST_NAME;
import static ro.axon.dot.EmployeeTestAttributes.ID;
import static ro.axon.dot.EmployeeTestAttributes.LAST_NAME;
import static ro.axon.dot.EmployeeTestAttributes.MDF_TMS;
import static ro.axon.dot.EmployeeTestAttributes.MDF_USR;
import static ro.axon.dot.EmployeeTestAttributes.ROLE;
import static ro.axon.dot.EmployeeTestAttributes.STATUS;
import static ro.axon.dot.EmployeeTestAttributes.USERNAME;
import static ro.axon.dot.EmployeeTestAttributes.V;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import ro.axon.dot.EmployeeTestAttributes;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.LeaveRequestEty;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.domain.TeamStatus;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsListItem;
import ro.axon.dot.model.LeaveRequestReview;
import ro.axon.dot.model.RegisterRequest;
import ro.axon.dot.model.RemainingDaysOff;
import ro.axon.dot.model.TeamDetails;
import ro.axon.dot.model.TeamDetailsListItem;
import ro.axon.dot.model.UserDetailsResponse;
import ro.axon.dot.security.JwtTokenUtil;
import ro.axon.dot.security.TokenUtilSetup;
import ro.axon.dot.service.EmployeeService;
import ro.axon.dot.service.LeaveRequestService;

@ExtendWith(MockitoExtension.class)
class EmployeeApiTest {

  private final String employeeId = EmployeeTestAttributes.ID;
  private final Long requestId = 1L;
  private final String editLeaveRequestContent = "{ \"startDate\": \"2023-08-25\", \"endDate\": \"2023-08-28\", \"type\": \"VACATION\", \"description\": \"Vacation leave request\", \"v\": 1 }";

  public static final TeamDetailsListItem teamDetails1 = new TeamDetailsListItem();
  public static final TeamDetailsListItem teamDetails2 = new TeamDetailsListItem();
  public static final EmployeeDetailsListItem employee = new EmployeeDetailsListItem();

  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  EmployeeService employeeService;
  @Mock
  LeaveRequestService leaveRequestService;

  @InjectMocks
  EmployeeApi employeeApi;

  MockMvc mockMvc;

  @BeforeEach
  void setUp(){
    mockMvc = MockMvcBuilders.standaloneSetup(employeeApi)
        .setControllerAdvice(new ApiExceptionHandler())
            .build();

    teamDetails1.setName("AxonTeam");
    teamDetails2.setName("InternshipTeam");

    employee.setId(ID);
    employee.setFirstName("John");
    employee.setLastName("Doe");
    employee.setTeamDetails(teamDetails1);
    employee.setTotalVacationDays(20);

  }

  @Test
  void getEmployeesList() throws Exception{

    EmployeeDetailsListItem employee1 = initEmployee();

    EmployeeDetailsListItem employee2 = initEmployee();
    employee2.setFirstName("Maria");
    employee2.setLastName("Anton");
    employee2.setTeamDetails(teamDetails2);
    employee2.setTotalVacationDays(21);
    EmployeeDetailsList employeesList = new EmployeeDetailsList();

    employeesList.setItems(Arrays.asList(employee1,employee2));

    when(employeeService.getEmployeesDetails(null)).thenReturn(employeesList);

    mockMvc.perform(get("/api/v1/employees")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath("$.items[0].firstName").value(FIRST_NAME))
        .andExpect(jsonPath("$.items[0].lastName").value(LAST_NAME))
        .andExpect(jsonPath("$.items[0].crtUsr").value(CRT_USR))
        .andExpect(jsonPath("$.items[0].mdfUsr").value(MDF_USR))
        .andExpect(jsonPath("$.items[0].v").value(V))
        .andExpect(jsonPath("$.items[0].username").value(USERNAME))
        .andExpect(jsonPath("$.items[0].totalVacationDays").value(21))
        .andExpect(jsonPath("$.items[0].teamDetails.name").value("AxonTeam"))
        .andExpect(jsonPath("$.items[1].firstName").value("Maria"))
        .andExpect(jsonPath("$.items[1].lastName").value("Anton"))
        .andExpect(jsonPath("$.items[1].crtUsr").value(CRT_USR))
        .andExpect(jsonPath("$.items[1].mdfUsr").value(MDF_USR))
        .andExpect(jsonPath("$.items[1].v").value(V))
        .andExpect(jsonPath("$.items[1].username").value(USERNAME))
        .andExpect(jsonPath("$.items[1].totalVacationDays").value(21))
        .andExpect(jsonPath("$.items[1].teamDetails.name").value("InternshipTeam"));
  }

  @Test
  void getEmployeesListNull() throws Exception{

    EmployeeDetailsList employeesList = new EmployeeDetailsList();

    when(employeeService.getEmployeesDetails(null)).thenReturn(employeesList);

    mockMvc.perform(get("/api/v1/employees")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isEmpty());
  }
  @Test
  void getEmployeesByNameNotFound() throws Exception {

    EmployeeDetailsList employeesList = new EmployeeDetailsList();

    when(employeeService.getEmployeesDetails(anyString())).thenReturn(employeesList);

    mockMvc.perform(get("/api/v1/employees")
            .param("name", "Radu")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isEmpty());
  }

  @Test
  void getEmployeesByName() throws Exception {

    EmployeeDetailsListItem employee = initEmployee();
    employee.setFirstName("Maria");
    employee.setLastName("Anton");
    employee.setTeamDetails(teamDetails2);
    employee.setTotalVacationDays(21);

    EmployeeDetailsList employeesList = new EmployeeDetailsList();

    employeesList.setItems(Arrays.asList(employee));

    when(employeeService.getEmployeesDetails(anyString())).thenReturn(employeesList);

    mockMvc.perform(get("/api/v1/employees")
            .param("name", "Anton")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].firstName").value("Maria"))
        .andExpect(jsonPath("$.items[0].lastName").value("Anton"))
        .andExpect(jsonPath("$.items[0].totalVacationDays").value(21))
        .andExpect(jsonPath("$.items[0].teamDetails.name").value("InternshipTeam"));
  }


  private String getJsonAnswer() throws IOException {
      LeaveRequestReview review = new LeaveRequestReview();
      review.setLeaveRequestStatus("APPROVED");
      review.setVersion(1L);
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(review);
  }


  @Test
  void answerLeaveRequestEmployeeNotFound() throws Exception {
      doThrow(new BusinessException(
              BusinessException.BusinessExceptionElement
                      .builder()
                      .errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND)
                      .build()))
              .when(employeeService).updateLeaveRequestStatus(anyLong(), anyLong(), any());

      mockMvc.perform(patch("/api/v1/employees/1/requests/1")
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON)
                      .content(getJsonAnswer()))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.message").value(BusinessErrorCode.EMPLOYEE_NOT_FOUND.getDevMsg()))
              .andExpect(jsonPath("$.errorCode").value(BusinessErrorCode.EMPLOYEE_NOT_FOUND.getErrorCode()));
  }

  @Test
  void answerLeaveRequestRequestNotFound() throws Exception {
      doThrow(new BusinessException(
              BusinessException.BusinessExceptionElement
                      .builder()
                      .errorDescription(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND)
                      .build()))
              .when(employeeService).updateLeaveRequestStatus(anyLong(), anyLong(), any());

      mockMvc.perform(patch("/api/v1/employees/1/requests/1")
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON)
                      .content(getJsonAnswer()))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.message").value(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND.getDevMsg()))
              .andExpect(jsonPath("$.errorCode").value(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND.getErrorCode()));
  }

  @Test
  void answerLeaveRequestRequestResolved() throws Exception {
      doThrow(new BusinessException(
              BusinessException.BusinessExceptionElement
                      .builder()
                      .errorDescription(BusinessErrorCode.LEAVE_REQUEST_REJECTED)
                      .build()))
              .when(employeeService).updateLeaveRequestStatus(anyLong(), anyLong(), any());

      mockMvc.perform(patch("/api/v1/employees/1/requests/1")
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON)
                      .content(getJsonAnswer()))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.message").value(BusinessErrorCode.LEAVE_REQUEST_REJECTED.getDevMsg()))
              .andExpect(jsonPath("$.errorCode").value(BusinessErrorCode.LEAVE_REQUEST_REJECTED.getErrorCode()));
  }
  @Test
  void answerLeaveRequestOutdatedVersion() throws Exception {
      doThrow(new BusinessException(
              BusinessException.BusinessExceptionElement
                      .builder()
                      .errorDescription(BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION)
                      .build()))
              .when(employeeService).updateLeaveRequestStatus(anyLong(), anyLong(), any());

      mockMvc.perform(patch("/api/v1/employees/1/requests/1")
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON)
                      .content(getJsonAnswer()))
              .andExpect(status().isConflict())
              .andExpect(jsonPath("$.message").value(BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION.getDevMsg()))
              .andExpect(jsonPath("$.errorCode").value(BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION.getErrorCode()));
  }

  @Test
  void answerLeaveRequestEmployeeOk() throws Exception {
      LeaveRequestReview review = new LeaveRequestReview();
      review.setLeaveRequestStatus("APPROVED");
      review.setVersion(1L);
      ObjectMapper mapper = new ObjectMapper();
      String jsonAnswer = mapper.writeValueAsString(review);

      LeaveRequestEty request = new LeaveRequestEty();
      request.setId(1L);
      request.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
      when(employeeService.updateLeaveRequestStatus(anyLong(), anyLong(), any())).thenReturn(request);

      mockMvc.perform(patch("/api/v1/employees/1/requests/1")
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON)
                      .content(jsonAnswer))
              .andExpect(status().isNoContent());
  }

  @Test
  void inactivateEmployeeSuccess() throws Exception {
    String employeeId = ID;

    mockMvc.perform(patch("/api/v1/employees/" + employeeId + "/inactivate"))
        .andExpect(status().isNoContent());
  }

  @Test
  void inactivateEmployeeThrowsBusinessException() throws Exception {
    String employeeId = ID;

    doThrow(new BusinessException(BusinessExceptionElement
        .builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()
    )).when(employeeService).inactivateEmployee(employeeId);

    mockMvc.perform(patch("/api/v1/employees/{employeeId}/inactivate", employeeId))
        .andExpect(status().isNotFound());

    verify(employeeService, times(1)).inactivateEmployee(employeeId);
  }

  @Test
  void getRemainingDaysOff() throws Exception {
    RemainingDaysOff remainingDaysOff = new RemainingDaysOff();
    remainingDaysOff.setRemainingDays(employee.getTotalVacationDays());

    when(employeeService.getEmployeeRemainingDaysOff(anyString())).thenReturn(remainingDaysOff);

    mockMvc.perform(get("/api/v1/employees/{employeeId}/remaining-days-off", ID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.remainingDays").value(20));
  }

  @Test
  void editLeaveRequestSucces() throws Exception {

    mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId, employeeId, requestId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(editLeaveRequestContent))
        .andExpect(status().isNoContent());
  }

  @Test
  public void testEditLeaveRequestEmployeeNotFound() throws Exception {

    when(employeeService.editLeaveRequest(anyString(), anyLong(), any()))
        .thenThrow( new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()));

    mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(editLeaveRequestContent))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testEditLeaveRequestNotFound() throws Exception {

    when(employeeService.editLeaveRequest(anyString(), anyLong(), any()))
        .thenThrow( new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND).build()));

    mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(editLeaveRequestContent))
        .andExpect(status().isNotFound());
  }
  @Test
  public void testEditLeaveRequestPastDate() throws Exception {

    when(employeeService.editLeaveRequest(anyString(), anyLong(), any()))
        .thenThrow( new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.LEAVE_REQUEST_PAST_DATE).build()));

    mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(editLeaveRequestContent))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testEditLeaveRequestRejected() throws Exception {

    when(employeeService.editLeaveRequest(anyString(), anyLong(), any()))
        .thenThrow( new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.LEAVE_REQUEST_REJECTED).build()));

    mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(editLeaveRequestContent))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testEditLeaveRequestPrecedingVersion() throws Exception {

    when(employeeService.editLeaveRequest(anyString(), anyLong(), any()))
        .thenThrow( new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION).build()));

    mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(editLeaveRequestContent))
        .andExpect(status().isConflict());
  }
  private EmployeeDetailsListItem initEmployee(){

    EmployeeDetailsListItem employee = new EmployeeDetailsListItem();
    employee.setId(ID);
    employee.setFirstName(FIRST_NAME);
    employee.setLastName(LAST_NAME);
    employee.setEmail(EMAIL);
    employee.setCrtUsr(CRT_USR);
    employee.setCrtTms(CRT_TMS);
    employee.setMdfUsr(MDF_USR);
    employee.setMdfTms(MDF_TMS);
    employee.setRole(ROLE);
    employee.setStatus(STATUS);
    employee.setV(V);
    employee.setUsername(USERNAME);
    employee.setTeamDetails(teamDetails1);
    employee.setTotalVacationDays(21);

    return employee;
  }

  @Test
  void getRemainingDaysOffEmployeeNotFound() throws Exception {
    RemainingDaysOff remainingDaysOff = new RemainingDaysOff();
    remainingDaysOff.setRemainingDays(employee.getTotalVacationDays());

    doThrow(new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()))
            .when(employeeService).getEmployeeRemainingDaysOff(ID);

    mockMvc.perform(get("/api/v1/employees/{employeeId}/remaining-days-off", ID)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(BusinessErrorCode.EMPLOYEE_NOT_FOUND.getErrorCode()));
  }

  @Test
  void getRemainingDaysOffYearlyDaysOffNotSet() throws Exception {
    RemainingDaysOff remainingDaysOff = new RemainingDaysOff();
    remainingDaysOff.setRemainingDays(employee.getTotalVacationDays());

    doThrow(new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.YEARLY_DAYS_OFF_NOT_SET).build()))
            .when(employeeService).getEmployeeRemainingDaysOff(ID);

    mockMvc.perform(get("/api/v1/employees/{employeeId}/remaining-days-off", ID)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(BusinessErrorCode.YEARLY_DAYS_OFF_NOT_SET.getErrorCode()));
  }

  @Test
  void checkEmployeeUniqueCredentials() throws Exception {
    when(employeeService.checkEmployeeUniqueCredentials(anyString(), anyString())).thenReturn(true);

    mockMvc.perform(get("/api/v1/employee/validation")
            .param("username", USERNAME)
            .param("email", EMAIL))
            .andExpect(status().isOk());
  }

  @Test
  void checkEmployeeUniqueCredentialsDuplicateUsername() throws Exception {
    doThrow(new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.USERNAME_ALREADY_EXISTS).build()
    )).when(employeeService).checkEmployeeUniqueCredentials(USERNAME, EMAIL);

    mockMvc.perform(get("/api/v1/employee/validation")
                    .param("username", USERNAME)
                    .param("email", EMAIL)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value(BusinessErrorCode.USERNAME_ALREADY_EXISTS.getErrorCode()));
  }

  @Test
  void checkEmployeeUniqueCredentialsDuplicateEmail() throws Exception {
    doThrow(new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.EMAIL_DUPLICATE).build()
    )).when(employeeService).checkEmployeeUniqueCredentials(USERNAME, EMAIL);

    mockMvc.perform(get("/api/v1/employee/validation")
                    .param("username", USERNAME)
                    .param("email", EMAIL)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value(BusinessErrorCode.EMAIL_DUPLICATE.getErrorCode()));
  }

    @Test
    void getLeaveRequestsOk() throws Exception {
        LeaveRequestDetailsList requestsDTO = new LeaveRequestDetailsList();
        LeaveRequestDetailsListItem request1 = new LeaveRequestDetailsListItem();
        request1.setId(1L);
        LeaveRequestDetailsListItem request2 = new LeaveRequestDetailsListItem();
        request2.setId(2L);
        requestsDTO.setItems(Arrays.asList(request1, request2));
        when(employeeService.getLeaveRequests(anyString(), any(), any())).thenReturn(requestsDTO);

        mockMvc.perform(get("/api/v1/employees/1/requests?startDate=2023-08-01&endDate=2023-08-11")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[1].id").value(2L));
    }

    @Test
    void getLeaveRequestsNoDatesOk() throws Exception {
        LeaveRequestDetailsList requestsDTO = new LeaveRequestDetailsList();
        LeaveRequestDetailsListItem request1 = new LeaveRequestDetailsListItem();
        request1.setId(1L);
        LeaveRequestDetailsListItem request2 = new LeaveRequestDetailsListItem();
        request2.setId(2L);
        requestsDTO.setItems(Arrays.asList(request1, request2));
        when(employeeService.getLeaveRequests("1", null, null)).thenReturn(requestsDTO);

        mockMvc.perform(get("/api/v1/employees/1/requests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[1].id").value(2L));
    }

    @Test
    void getLeaveRequestsEmployeeNotFound() throws Exception {
        doThrow(new BusinessException(
                BusinessException.BusinessExceptionElement
                        .builder()
                        .errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND)
                        .build()))
                .when(employeeService).getLeaveRequests(anyString(), any(), any());

        mockMvc.perform(get("/api/v1/employees/1/requests")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value(BusinessErrorCode.EMPLOYEE_NOT_FOUND.getDevMsg()))
                        .andExpect(jsonPath("$.errorCode").value(BusinessErrorCode.EMPLOYEE_NOT_FOUND.getErrorCode()));
    }
}