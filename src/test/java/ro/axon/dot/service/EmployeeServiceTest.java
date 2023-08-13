package ro.axon.dot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.domain.TeamStatus;
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
  EmployeeService employeeService;
  EmployeeMapper employeeMapper;
  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    employeeMapper = new EmployeeMapperImpl();
    employeeService = new EmployeeService(employeeRepository, passwordEncoder);

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
  void createEmployee() {

    TeamEty team = new TeamEty();
    team.setId(1L);
    team.setName("Backend");
    team.setCrtUsr("crtUsr");
    team.setCrtTms(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    team.setMdfUsr("mdfUsr");
    team.setMdfTms(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    team.setStatus(TeamStatus.ACTIVE);

    EmployeeEty employee = new EmployeeEty(
        "11",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "mdfUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "role.user",
        "status.active",
        LocalDate.now(),
        LocalDate.now(),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        team,
        new HashSet<>()
        );

    when(employeeRepository.save(any())).thenReturn(employee);


    EmployeeDetailsListItem returnedEmployee = employeeService.createEmployee(employeeMapper.mapEmployeeEtyToEmployeeDto(employee));

    EmployeeEty returned = employeeMapper.mapEmployeeDtoToEmployeeEty(returnedEmployee);

    assertNotNull(returned);
    assertEquals(employee.getId(), returned.getId());
    assertEquals(employee.getFirstName(), returned.getFirstName());
    assertEquals(employee.getLastName(), returned.getLastName());
    assertEquals(employee.getEmail(), returned.getEmail());
    assertEquals(employee.getCrtUsr(), returned.getCrtUsr());
    assertEquals(employee.getCrtTms(), returned.getCrtTms());
    assertEquals(employee.getMdfUsr(), returned.getMdfUsr());
    assertEquals(employee.getMdfTms(), returned.getMdfTms());
    assertEquals(employee.getRole(), returned.getRole());
    assertEquals(employee.getStatus(), returned.getStatus());
    assertEquals(employee.getContractStartDate(), returned.getContractStartDate());
    assertEquals(employee.getUsername(), returned.getUsername());
    assertEquals(employee.getTeam(), returned.getTeam());
    assertTrue(passwordEncoder.matches("axon_" + returned.getUsername(), employee.getPassword()));

  }

  @Test
  void loadEmployeeByUsername() {

    TeamEty team = new TeamEty();
    team.setId(1L);
    team.setName("Backend");
    team.setCrtUsr("crtUsr");
    team.setCrtTms(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    team.setMdfUsr("mdfUsr");
    team.setMdfTms(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    team.setStatus(TeamStatus.ACTIVE);

    EmployeeEty employee = new EmployeeEty(
        "12",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "mdfUsr",
        LocalDateTime.now().toInstant(ZoneOffset.UTC),
        "role.user",
        "status.active",
        LocalDate.now(),
        LocalDate.now(),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        team,
        new HashSet<>()
    );

    when(employeeRepository.findEmployeeByUsername(any())).thenReturn(Optional.of(employee));

    EmployeeEty loadedEmployee;

    try {
      loadedEmployee = employeeService.loadEmployeeByUsername("test.user2");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertNotNull(loadedEmployee);
    assertEquals(employee.getId(), loadedEmployee.getId());
    assertEquals(employee.getFirstName(), loadedEmployee.getFirstName());
    assertEquals(employee.getLastName(), loadedEmployee.getLastName());
    assertEquals(employee.getEmail(), loadedEmployee.getEmail());
    assertEquals(employee.getCrtUsr(), loadedEmployee.getCrtUsr());
    assertEquals(employee.getCrtTms(), loadedEmployee.getCrtTms());
    assertEquals(employee.getMdfUsr(), loadedEmployee.getMdfUsr());
    assertEquals(employee.getMdfTms(), loadedEmployee.getMdfTms());
    assertEquals(employee.getRole(), loadedEmployee.getRole());
    assertEquals(employee.getStatus(), loadedEmployee.getStatus());
    assertEquals(employee.getContractStartDate(), loadedEmployee.getContractStartDate());
    assertEquals(employee.getUsername(), loadedEmployee.getUsername());
    assertEquals(employee.getTeam(), loadedEmployee.getTeam());
  }
}