package ro.axon.dot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.RemainingDaysOff;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

  EmployeeService employeeService;

  @Mock
  EmployeeRepository employeeRepository;

  @BeforeEach
  void setUp() {
    employeeService = new EmployeeService(employeeRepository);

    TEAM_ETY.setId(1L);
    TEAM_ETY.setName("AxonTeam");
    TEAM_ETY.setCrtTms(CRT_TMS);
    TEAM_ETY.setMdfUsr(MDF_USR);
    TEAM_ETY.setMdfTms(MDF_TMS);
  }

  @Test
  void getEmployeesDetails() throws Exception{

    EmployeeEty employee = initEmployee();

    List<EmployeeEty> employees = Arrays.asList(employee, new EmployeeEty(), new EmployeeEty());

    when(employeeRepository.findAll()).thenReturn(employees);

    EmployeeDetailsList returnedEmployeesList = employeeService.getEmployeesDetails(null);

    assertEquals(3, returnedEmployeesList.getItems().size());

    EmployeeDetailsListItem returnedEmployee = returnedEmployeesList.getItems().get(0);
    assertEquals(ID, returnedEmployee.getId());
    assertEquals(FIRST_NAME, returnedEmployee.getFirstName());
    assertEquals(LAST_NAME, returnedEmployee.getLastName());
    assertEquals(EMAIL, returnedEmployee.getEmail());
    assertEquals(CRT_USR, returnedEmployee.getCrtUsr());
    assertEquals(CRT_TMS, returnedEmployee.getCrtTms());
    assertEquals(MDF_USR, returnedEmployee.getMdfUsr());
    assertEquals(MDF_TMS, returnedEmployee.getMdfTms());
    assertEquals(ROLE, returnedEmployee.getRole());
    assertEquals(STATUS, returnedEmployee.getStatus());
    assertEquals(CONTRACT_START_DATE, returnedEmployee.getContractStartDate());
    assertEquals(USERNAME, returnedEmployee.getUsername());

    assertEquals(TEAM_ETY.getId(), returnedEmployee.getTeamDetails().getId());
    assertEquals(TEAM_ETY.getName(), returnedEmployee.getTeamDetails().getName());
    assertEquals(TEAM_ETY.getCrtUsr(), returnedEmployee.getTeamDetails().getCrtUsr());
    assertEquals(TEAM_ETY.getMdfUsr(), returnedEmployee.getTeamDetails().getMdfUsr());
    assertEquals(TEAM_ETY.getMdfTms(), returnedEmployee.getTeamDetails().getMdfTms());
  }

  @Test
  void getEmployeeByName() {
    String searchName = "Cris";

    EmployeeEty employee1 = initEmployee();
    employee1.setFirstName("Cristian");

    EmployeeEty employee2 = initEmployee();
    employee2.setLastName("Cristurean");

    List<EmployeeEty> employees = Arrays.asList(employee1, employee2);

    when(employeeRepository.findAll())
        .thenReturn(employees.stream()
            .filter(e -> e.getFirstName().contains(searchName) || e.getLastName().contains(searchName))
            .collect(Collectors.toList()));

    EmployeeDetailsList returnedEmployees = employeeService.getEmployeesDetails(searchName);

    assertEquals(2, returnedEmployees.getItems().size());
    verify(employeeRepository, times(1)).findAll();
  }

  @Test
  void inactivateEmployeeSuccess(){
    EmployeeEty employee = initEmployee();

    when(employeeRepository.findById(ID)).thenReturn(Optional.of(employee));

    employeeService.inactivateEmployee(ID);

    assertEquals("INACTIVE", employee.getStatus());
    assertEquals("User", employee.getMdfUsr());
  }

  @Test
  void inactivateEmployeeFail(){

    when(employeeRepository.findById(ID)).thenReturn(Optional.empty());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> employeeService.inactivateEmployee(ID));

    assertEquals(BusinessErrorCode.EMPLOYEE_NOT_FOUND, exception.getError().getErrorDescription());
    verify(employeeRepository, never()).save(any());
  }

  private EmployeeEty initEmployee(){

    EmployeeEty employee = new EmployeeEty();

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
    employee.setContractStartDate(CONTRACT_START_DATE);
    employee.setContractEndDate(CONTRACT_END_DATE);
    employee.setUsername(USERNAME);
    employee.setTeam(TEAM_ETY);

    return employee;
  }

  @Test
  void getEmployeeRemainingDaysOffIdNotFound() {
    EmployeeEty employee = new EmployeeEty();
    EmpYearlyDaysOffEty daysOff = new EmpYearlyDaysOffEty();
    daysOff.setId(1L);
    daysOff.setTotalNoDays(20);
    daysOff.setYear(2023);
    employee.setId(ID);
    employee.setEmpYearlyDaysOff(Collections.singleton(daysOff));

    when(employeeRepository.findById(anyString())).thenReturn(Optional.empty());

    try {
      RemainingDaysOff remainingDaysOff = employeeService.getEmployeeRemainingDaysOff(ID);
    } catch (BusinessException businessException) {
      assertEquals("The employee with the given ID does not exist.", businessException.getError().getErrorDescription().getDevMsg());
      return;
    }
    fail();
  }

  @Test
  void getEmployeeRemainingDaysOffNotSet() {
    EmployeeEty employee = new EmployeeEty();
    employee.setId(ID);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    try {
      RemainingDaysOff remainingDaysOff = employeeService.getEmployeeRemainingDaysOff(ID);
    } catch (BusinessException businessException) {
      assertEquals("The vacation days for this employee have not been set for this year.", businessException.getError().getErrorDescription().getDevMsg());
      return;
    }
    fail();
  }

  @Test
  void getEmployeeRemainingDaysOff() {
    EmployeeEty employee = new EmployeeEty();
    EmpYearlyDaysOffEty daysOff = new EmpYearlyDaysOffEty();
    daysOff.setId(1L);
    daysOff.setTotalNoDays(20);
    daysOff.setYear(2023);
    employee.setId(ID);
    employee.setEmpYearlyDaysOff(Collections.singleton(daysOff));

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    RemainingDaysOff remainingDaysOff = employeeService.getEmployeeRemainingDaysOff(ID);

    assertEquals(20, remainingDaysOff.getRemainingDays());
  }

}