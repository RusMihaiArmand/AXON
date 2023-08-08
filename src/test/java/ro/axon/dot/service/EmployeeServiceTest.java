package ro.axon.dot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.TeamRepository;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.mapper.EmployeeMapperImpl;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

  PasswordEncoder passwordEncoder;
  @Mock
  EmployeeRepository employeeRepository;
  @Mock
  TeamRepository teamRepository;

  EmployeeService employeeService;

  EmployeeMapper employeeMapper;

  EmployeeEty testEmployee;
  @BeforeEach
  void setUp() {

    passwordEncoder = new BCryptPasswordEncoder();
    employeeMapper = new EmployeeMapperImpl();

    employeeService = new EmployeeService(employeeRepository, passwordEncoder, teamRepository);

    TEAM_ETY.setId(1L);
    TEAM_ETY.setName("AxonTeam");
    TEAM_ETY.setCrtTms(CRT_TMS);
    TEAM_ETY.setMdfUsr(MDF_USR);
    TEAM_ETY.setMdfTms(MDF_TMS);

    testEmployee = initEmployee();
    testEmployee.setPassword("$2a$10$5d4MyhXzP1n6kq6ysW2kle00a0nZmWM1UF5qtFum25Ipi/umATCoe");
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
  void createEmployee() {

    when(employeeRepository.save(any())).thenReturn(testEmployee);
    when(teamRepository.findById(any())).thenReturn(Optional.of(TEAM_ETY));


    EmployeeDetailsListItem returnedEmployee = employeeService.createEmployee(employeeMapper.mapEmployeeEtyToEmployeeDto(testEmployee));

    EmployeeEty returned = employeeMapper.mapEmployeeDtoToEmployeeEty(returnedEmployee);

    assertNotNull(returned);
    assertEquals(testEmployee.getId(), returned.getId());
    assertEquals(testEmployee.getFirstName(), returned.getFirstName());
    assertEquals(testEmployee.getLastName(), returned.getLastName());
    assertEquals(testEmployee.getEmail(), returned.getEmail());
    assertEquals(testEmployee.getCrtUsr(), returned.getCrtUsr());
    assertEquals(testEmployee.getCrtTms(), returned.getCrtTms());
    assertEquals(testEmployee.getMdfUsr(), returned.getMdfUsr());
    assertEquals(testEmployee.getMdfTms(), returned.getMdfTms());
    assertEquals(testEmployee.getRole(), returned.getRole());
    assertEquals(testEmployee.getStatus(), returned.getStatus());
    assertEquals(testEmployee.getContractStartDate(), returned.getContractStartDate());
    assertEquals(testEmployee.getUsername(), returned.getUsername());
    assertEquals(testEmployee.getTeam(), returned.getTeam());
    assertTrue(passwordEncoder.matches("axon_" + returned.getUsername(), testEmployee.getPassword()));

  }

  @Test
  void loadEmployeeByUsername() {

    when(employeeRepository.findEmployeeByUsername(any())).thenReturn(Optional.of(testEmployee));
    when(teamRepository.findById(any())).thenReturn(Optional.of(TEAM_ETY));

    EmployeeEty loadedEmployee;

    try {
      loadedEmployee = employeeService.loadEmployeeByUsername("test.user2");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertNotNull(loadedEmployee);
    assertEquals(testEmployee.getId(), loadedEmployee.getId());
    assertEquals(testEmployee.getFirstName(), loadedEmployee.getFirstName());
    assertEquals(testEmployee.getLastName(), loadedEmployee.getLastName());
    assertEquals(testEmployee.getEmail(), loadedEmployee.getEmail());
    assertEquals(testEmployee.getCrtUsr(), loadedEmployee.getCrtUsr());
    assertEquals(testEmployee.getCrtTms(), loadedEmployee.getCrtTms());
    assertEquals(testEmployee.getMdfUsr(), loadedEmployee.getMdfUsr());
    assertEquals(testEmployee.getMdfTms(), loadedEmployee.getMdfTms());
    assertEquals(testEmployee.getRole(), loadedEmployee.getRole());
    assertEquals(testEmployee.getStatus(), loadedEmployee.getStatus());
    assertEquals(testEmployee.getContractStartDate(), loadedEmployee.getContractStartDate());
    assertEquals(testEmployee.getUsername(), loadedEmployee.getUsername());
    assertEquals(testEmployee.getTeam(), loadedEmployee.getTeam());
  }
}