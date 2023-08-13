package ro.axon.dot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;
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
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.domain.TeamStatus;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.mapper.EmployeeMapperImpl;
import ro.axon.dot.domain.LeaveRequestEty;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;
import ro.axon.dot.domain.LeaveRequestRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.LeaveRequestDetailsListItem;
import ro.axon.dot.model.RemainingDaysOff;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

  PasswordEncoder passwordEncoder;
  @Mock
  EmployeeRepository employeeRepository;
  EmployeeService employeeService;
  EmployeeMapper employeeMapper;
  @Mock
  LeaveRequestRepository leaveRequestRepository;

  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    employeeMapper = new EmployeeMapperImpl();
    employeeService = new EmployeeService(employeeRepository, leaveRequestRepository, passwordEncoder);

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

    var ex = assertThrows(BusinessException.class, () -> { employeeService.getEmployeeRemainingDaysOff(ID);});
    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.EMPLOYEE_NOT_FOUND);
  }

  @Test
  void getEmployeeRemainingDaysOffNotSet() {
    EmployeeEty employee = new EmployeeEty();
    employee.setId(ID);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    var ex = assertThrows(BusinessException.class, () -> { employeeService.getEmployeeRemainingDaysOff(ID);});
    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.YEARLY_DAYS_OFF_NOT_SET);
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

  @Test
  void editLeaveRequestNotFound(){

    EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
    leaveRequestEdit.setStartDate(LocalDate.parse("2023-08-25"));
    leaveRequestEdit.setEndDate(LocalDate.parse("2023-08-28"));
    leaveRequestEdit.setType(LeaveRequestEtyTypeEnum.VACATION);
    leaveRequestEdit.setDescription("Vacation leave request");

    EmployeeEty employee = new EmployeeEty();
    employee.setId(ID);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> employeeService.editLeaveRequest(ID, 1L, leaveRequestEdit));

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND, exception.getError().getErrorDescription());
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  void editLeaveRequestRejected(){

    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    Long leaveRequestIdValue = 1L;
    leaveRequest.setId(leaveRequestIdValue);
    leaveRequest.setStatus(LeaveRequestEtyStatusEnum.REJECTED);
    leaveRequest.setV(1L);

    EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
    leaveRequestEdit.setStartDate(LocalDate.parse("2023-08-25"));
    leaveRequestEdit.setEndDate(LocalDate.parse("2023-08-28"));
    leaveRequestEdit.setType(LeaveRequestEtyTypeEnum.VACATION);
    leaveRequestEdit.setDescription("Vacation leave request");
    leaveRequestEdit.setV(2L);

    EmployeeEty employee = new EmployeeEty();
    employee.setId(ID);
    employee.getLeaveRequests().add(leaveRequest);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> employeeService.editLeaveRequest(ID, leaveRequestIdValue, leaveRequestEdit));

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_REJECTED, exception.getError().getErrorDescription());
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  void editLeaveRequestPastDate(){

    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    Long leaveRequestIdValue = 1L;
    leaveRequest.setId(leaveRequestIdValue);
    leaveRequest.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
    leaveRequest.setStartDate(LocalDate.parse("2023-08-23"));
    leaveRequest.setEndDate(LocalDate.parse("2023-08-27"));
    leaveRequest.setV(1L);

    EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
    leaveRequestEdit.setStartDate(LocalDate.parse("2023-07-25"));
    leaveRequestEdit.setEndDate(LocalDate.parse("2023-07-28"));
    leaveRequestEdit.setType(LeaveRequestEtyTypeEnum.VACATION);
    leaveRequestEdit.setDescription("Vacation leave request");
    leaveRequestEdit.setV(2L);

    EmployeeEty employee = new EmployeeEty();
    employee.setId(ID);
    employee.getLeaveRequests().add(leaveRequest);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> employeeService.editLeaveRequest(ID, leaveRequestIdValue, leaveRequestEdit));

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_PAST_DATE, exception.getError().getErrorDescription());
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  void editLeaveRequestPrecedingVersion(){

    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    Long leaveRequestIdValue = 1L;
    leaveRequest.setId(leaveRequestIdValue);
    leaveRequest.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
    leaveRequest.setStartDate(LocalDate.parse("2023-08-23"));
    leaveRequest.setEndDate(LocalDate.parse("2023-08-27"));
    leaveRequest.setV(2L);

    EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
    leaveRequestEdit.setStartDate(LocalDate.parse("2023-08-25"));
    leaveRequestEdit.setEndDate(LocalDate.parse("2023-08-28"));
    leaveRequestEdit.setType(LeaveRequestEtyTypeEnum.VACATION);
    leaveRequestEdit.setDescription("Vacation leave request");
    leaveRequestEdit.setV(1L);

    EmployeeEty employee = new EmployeeEty();
    employee.setId(ID);
    employee.getLeaveRequests().add(leaveRequest);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> employeeService.editLeaveRequest(ID, leaveRequestIdValue, leaveRequestEdit));

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION, exception.getError().getErrorDescription());
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  void editLeaveRequestSuccess(){

    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    Long leaveRequestIdValue = 1L;
    leaveRequest.setId(leaveRequestIdValue);
    leaveRequest.setType(LeaveRequestEtyTypeEnum.MEDICAL);
    leaveRequest.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
    leaveRequest.setStartDate(LocalDate.parse("2023-08-23"));
    leaveRequest.setEndDate(LocalDate.parse("2023-08-27"));
    leaveRequest.setDescription("Medical leave request");
    leaveRequest.setV(1L);

    EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
    leaveRequestEdit.setStartDate(LocalDate.parse("2023-08-25"));
    leaveRequestEdit.setEndDate(LocalDate.parse("2023-08-28"));
    leaveRequestEdit.setType(LeaveRequestEtyTypeEnum.VACATION);
    leaveRequestEdit.setDescription("Vacation leave request");
    leaveRequestEdit.setV(1L);

    EmployeeEty employee = new EmployeeEty();
    employee.setId(ID);
    employee.getLeaveRequests().add(leaveRequest);

    LeaveRequestEty savedLeaveRequest = new LeaveRequestEty();
    savedLeaveRequest.setId(leaveRequestIdValue);
    savedLeaveRequest.setType(LeaveRequestEtyTypeEnum.VACATION);
    savedLeaveRequest.setStartDate(LocalDate.parse("2023-08-25"));
    savedLeaveRequest.setEndDate(LocalDate.parse("2023-08-28"));
    savedLeaveRequest.setDescription("Vacation leave request");
    savedLeaveRequest.setStatus(LeaveRequestEtyStatusEnum.PENDING);
    savedLeaveRequest.setV(1L);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));
    when(leaveRequestRepository.save(any())).thenReturn(savedLeaveRequest);

    LeaveRequestDetailsListItem leaveRequestItem = employeeService.editLeaveRequest(ID,
        leaveRequestIdValue,
        leaveRequestEdit);

    assertEquals(leaveRequestEdit.getStartDate(), leaveRequestItem.getStartDate());
    assertEquals(leaveRequestEdit.getEndDate(), leaveRequestItem.getEndDate());
    assertEquals(leaveRequestEdit.getType(), leaveRequestItem.getType());
    assertEquals(leaveRequestEdit.getDescription(), leaveRequestItem.getDescription());
    assertEquals(LeaveRequestEtyStatusEnum.PENDING, leaveRequestItem.getStatus());
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