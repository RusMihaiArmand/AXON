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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;

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

    EmployeeEty employee1 = new EmployeeEty();

    employee1.setId(ID);
    employee1.setFirstName("Cristian");
    employee1.setLastName(LAST_NAME);
    employee1.setEmail(EMAIL);
    employee1.setCrtUsr(CRT_USR);
    employee1.setCrtTms(CRT_TMS);
    employee1.setMdfUsr(MDF_USR);
    employee1.setMdfTms(MDF_TMS);
    employee1.setRole(ROLE);
    employee1.setStatus(STATUS);
    employee1.setContractStartDate(CONTRACT_START_DATE);
    employee1.setContractEndDate(CONTRACT_END_DATE);
    employee1.setUsername(USERNAME);
    employee1.setTeam(TEAM_ETY);

    EmployeeEty employee2 = new EmployeeEty();

    employee2.setId(ID);
    employee2.setFirstName(FIRST_NAME);
    employee2.setLastName("Cristurean");
    employee2.setEmail(EMAIL);
    employee2.setCrtUsr(CRT_USR);
    employee2.setCrtTms(CRT_TMS);
    employee2.setMdfUsr(MDF_USR);
    employee2.setMdfTms(MDF_TMS);
    employee2.setRole(ROLE);
    employee2.setStatus(STATUS);
    employee2.setContractStartDate(CONTRACT_START_DATE);
    employee2.setContractEndDate(CONTRACT_END_DATE);
    employee2.setUsername(USERNAME);
    employee2.setTeam(TEAM_ETY);

    List<EmployeeEty> employees = Arrays.asList(employee1, employee2);

    when(employeeRepository.findAll())
        .thenReturn(employees.stream()
            .filter(e -> e.getFirstName().contains(searchName) || e.getLastName().contains(searchName))
            .collect(Collectors.toList()));

    EmployeeDetailsList returnedEmployees = employeeService.getEmployeesDetails(searchName);

    assertEquals(1, returnedEmployees.getItems().size());
    verify(employeeRepository, times(1)).findAll();
  }

}