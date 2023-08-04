package ro.axon.dot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;

class EmployeeServiceTest {

  EmployeeService employeeService;

  @Mock
  EmployeeRepository employeeRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);

    employeeService = new EmployeeService(employeeRepository);

    TEAM_ETY.setId(1L);
    TEAM_ETY.setName("AxonTeam");
    TEAM_ETY.setCrtTms(CRT_TMS);
    TEAM_ETY.setMdfUsr(MDF_USR);
    TEAM_ETY.setMdfTms(MDF_TMS);
  }

  @Test
  void getEmployeesDetails() throws Exception{

    EmployeeEty employee = new EmployeeEty(ID, FIRST_NAME, LAST_NAME, EMAIL, CRT_USR, CRT_TMS,
        MDF_USR, MDF_TMS, ROLE, STATUS, CONTRACT_START_DATE, CONTRACT_END_DATE,
        V, USERNAME, TEAM_ETY);

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
    assertEquals(V, returnedEmployee.getV());
    assertEquals(USERNAME, returnedEmployee.getUsername());

    assertEquals(TEAM_ETY.getId(), returnedEmployee.getTeamDetails().getId());
    assertEquals(TEAM_ETY.getName(), returnedEmployee.getTeamDetails().getName());
    assertEquals(TEAM_ETY.getCrtUsr(), returnedEmployee.getTeamDetails().getCrtUsr());
    assertEquals(TEAM_ETY.getMdfUsr(), returnedEmployee.getTeamDetails().getMdfUsr());
    assertEquals(TEAM_ETY.getMdfTms(), returnedEmployee.getTeamDetails().getMdfTms());
  }

  @Test
  void getEmployeeByName() {
    String searchName = "Pop";

    EmployeeEty employee1 = new EmployeeEty(ID, FIRST_NAME, "Pop", EMAIL, CRT_USR, CRT_TMS,
        MDF_USR, MDF_TMS, ROLE, STATUS, CONTRACT_START_DATE, CONTRACT_END_DATE,
        V, USERNAME, TEAM_ETY);

    EmployeeEty employee2 = new EmployeeEty(ID, FIRST_NAME, "Popa", EMAIL, CRT_USR, CRT_TMS,
        MDF_USR, MDF_TMS, ROLE, STATUS, CONTRACT_START_DATE, CONTRACT_END_DATE,
        V, USERNAME, TEAM_ETY);

    EmployeeEty employee3 = new EmployeeEty(ID, FIRST_NAME, "Dan", EMAIL, CRT_USR, CRT_TMS,
        MDF_USR, MDF_TMS, ROLE, STATUS, CONTRACT_START_DATE, CONTRACT_END_DATE,
        V, USERNAME, TEAM_ETY);

    List<EmployeeEty> employees = Arrays.asList(employee1, employee2, employee3);

    when(employeeRepository.findEmployeeByLastNameContainingIgnoreCase(searchName))
        .thenReturn(employees.stream()
            .filter(e -> e.getFirstName().contains(searchName) || e.getLastName().contains(searchName))
            .collect(Collectors.toList()));

    EmployeeDetailsList returnedEmployees = employeeService.getEmployeesDetails(searchName);

    assertEquals(2, returnedEmployees.getItems().size());
    verify(employeeRepository, times(1))
        .findEmployeeByLastNameContainingIgnoreCase(searchName);
  }

}