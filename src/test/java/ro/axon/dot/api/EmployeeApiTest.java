package ro.axon.dot.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.axon.dot.EmployeeTestAttributes;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.RemainingDaysOff;
import ro.axon.dot.model.TeamDetailsListItem;
import ro.axon.dot.service.EmployeeService;
import ro.axon.dot.service.LeaveRequestService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.axon.dot.EmployeeTestAttributes.*;

@ExtendWith(MockitoExtension.class)
class EmployeeApiTest {

  private final String employeeId = EmployeeTestAttributes.ID;
  private final Long requestId = 1L;
  private final String editLeaveRequestContent = "{ \"startDate\": \"2023-08-25\", \"endDate\": \"2023-08-28\", \"type\": \"VACATION\", \"description\": \"Vacation leave request\", \"v\": 1 }";

  public static final TeamDetailsListItem teamDetails1 = new TeamDetailsListItem();
  public static final TeamDetailsListItem teamDetails2 = new TeamDetailsListItem();
  public static final EmployeeDetailsListItem employee = new EmployeeDetailsListItem();


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
        .andExpect(status().isBadRequest());

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
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testEditLeaveRequestNotFound() throws Exception {

    when(employeeService.editLeaveRequest(anyString(), anyLong(), any()))
        .thenThrow( new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND).build()));

    mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(editLeaveRequestContent))
        .andExpect(status().isBadRequest());
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
}