package ro.axon.dot.api;

import static org.hamcrest.Matchers.hasSize;
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
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.RemainingDaysOff;
import ro.axon.dot.model.TeamDetailsListItem;
import ro.axon.dot.service.EmployeeService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ro.axon.dot.EmployeeTestAttributes.*;

@ExtendWith(MockitoExtension.class)
class EmployeeApiTest {
  public static final TeamDetailsListItem teamDetails1 = new TeamDetailsListItem();
  public static final TeamDetailsListItem teamDetails2 = new TeamDetailsListItem();
  public static final EmployeeDetailsListItem employee1 = new EmployeeDetailsListItem();
  public static final EmployeeDetailsListItem employee2 = new EmployeeDetailsListItem();
  public static final EmployeeDetailsListItem employee3 = new EmployeeDetailsListItem();
  public static final EmployeeDetailsList employeesList = new EmployeeDetailsList();


  @Mock
  EmployeeService employeeService;

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

    employee1.setFirstName(FIRST_NAME);
    employee1.setLastName(LAST_NAME);
    employee1.setEmail(EMAIL);
    employee1.setCrtUsr(CRT_USR);
    employee1.setCrtTms(CRT_TMS);
    employee1.setMdfUsr(MDF_USR);
    employee1.setMdfTms(MDF_TMS);
    employee1.setRole(ROLE);
    employee1.setStatus(STATUS);
    employee1.setV(V);
    employee1.setUsername(USERNAME);
    employee1.setTeamDetails(teamDetails1);
    employee1.setTotalVacationDays(21);

    employee2.setFirstName("Maria");
    employee2.setLastName("Anton");
    employee2.setTeamDetails(teamDetails2);
    employee2.setTotalVacationDays(21);

    employee3.setId(ID);
    employee3.setFirstName("John");
    employee3.setLastName("Doe");
    employee3.setTeamDetails(teamDetails1);
    employee3.setTotalVacationDays(20);
  }

  @Test
  void getEmployeesList() throws Exception{

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
        .andExpect(jsonPath("$.items[1].totalVacationDays").value(21))
        .andExpect(jsonPath("$.items[1].teamDetails.name").value("InternshipTeam"));
  }

  @Test
  void getEmployeesListNull() throws Exception{
    when(employeeService.getEmployeesDetails(null)).thenReturn(employeesList);

    mockMvc.perform(get("/api/v1/employees")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isEmpty());
  }
  @Test
  void getEmployeesByNameNotFound() throws Exception {

    when(employeeService.getEmployeesDetails(anyString())).thenReturn(employeesList);

    mockMvc.perform(get("/api/v1/employees")
            .param("name", "Radu")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isEmpty());
  }

  @Test
  void getEmployeesByName() throws Exception {
    employeesList.setItems(Arrays.asList(employee2));

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
    remainingDaysOff.setRemainingDays(employee3.getTotalVacationDays());

    when(employeeService.getEmployeeRemainingDaysOff(anyString())).thenReturn(remainingDaysOff);

    mockMvc.perform(get("/api/v1/employees/{employeeId}/remaining-days-off", ID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.remainingDays").value(20));
  }
}