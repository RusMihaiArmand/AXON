package ro.axon.dot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.CONTRACT_END_DATE;
import static ro.axon.dot.EmployeeTestAttributes.CONTRACT_START_DATE;
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
import static ro.axon.dot.EmployeeTestAttributes.TEAM_ETY;
import static ro.axon.dot.EmployeeTestAttributes.USERNAME;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.axon.dot.config.component.JwtTokenUtil;
import ro.axon.dot.domain.entity.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.entity.EmployeeEty;
import ro.axon.dot.domain.entity.LeaveRequestEty;
import ro.axon.dot.domain.enums.LeaveRequestStatus;
import ro.axon.dot.domain.enums.LeaveRequestType;
import ro.axon.dot.domain.entity.TeamEty;
import ro.axon.dot.domain.enums.TeamStatus;
import ro.axon.dot.domain.enums.VacationDaysChangeType;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.mapper.EmployeeMapperImpl;
import ro.axon.dot.model.CreateLeaveRequestDetails;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.EmployeeUpdateRequest;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsListItem;
import ro.axon.dot.model.LeaveRequestReview;
import ro.axon.dot.model.LegallyDaysOffList;
import ro.axon.dot.model.RegisterRequest;
import ro.axon.dot.model.RemainingDaysOff;
import ro.axon.dot.model.TeamDetails;
import ro.axon.dot.model.UserDetailsResponse;
import ro.axon.dot.model.VacationDaysModifyDetails;
import ro.axon.dot.domain.repositories.EmployeeRepository;
import ro.axon.dot.domain.repositories.LeaveRequestRepository;
import ro.axon.dot.domain.repositories.TeamRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

  @InjectMocks
  EmployeeService employeeService;
  @Mock
  JwtTokenUtil tokenUtil;
  @Mock
  EmployeeRepository employeeRepository;
  @Mock
  LeaveRequestRepository leaveRequestRepository;
  @Mock
  LegallyDaysOffService legallyDaysOffService;
  @Mock
  TeamRepository teamRepository;

  PasswordEncoder passwordEncoder;
  EmployeeMapper employeeMapper;
  Clock clock;

  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    employeeMapper = new EmployeeMapperImpl();
    clock = Clock.systemDefaultZone();

    employeeService = new EmployeeService(employeeRepository, teamRepository,
        leaveRequestRepository, legallyDaysOffService, passwordEncoder, tokenUtil, clock);

    TEAM_ETY.setId(1L);
    TEAM_ETY.setName("AxonTeam");
    TEAM_ETY.setCrtTms(CRT_TMS);
    TEAM_ETY.setMdfUsr(MDF_USR);
    TEAM_ETY.setMdfTms(MDF_TMS);
  }

  @Test
  void getEmployeesDetails() {

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


    assertNull(returnedEmployeesList.getItems().get(1).getId());
    assertNull(returnedEmployeesList.getItems().get(2).getId());

    verify(employeeRepository, times(1)).findAll();
  }

  @Test
  void getEmployeeByName() {
    String searchName = "Cris";

    EmployeeEty employee1 = initEmployee();
    employee1.setFirstName("Cristian");

    EmployeeEty employee2 = initEmployee();
    employee2.setLastName("Cristurean");

    List<EmployeeEty> employees = Arrays.asList(employee1, employee2);

    when(employeeRepository.findAll()).thenReturn(employees);
    EmployeeDetailsList returnedEmployees = employeeService.getEmployeesDetails(searchName);

    assertEquals(2, returnedEmployees.getItems().size());
    assertEquals(returnedEmployees.getItems().get(0).getFirstName(), "Cristian");
    assertEquals(returnedEmployees.getItems().get(1).getLastName(), "Cristurean");
    verify(employeeRepository, times(1)).findAll();
  }


  @Test
  void updateLeaveRequestStatusEmployeeNotFound() {
    LeaveRequestReview review = new LeaveRequestReview();
    review.setType("APPROVED");
    review.setV(1L);
    when(employeeRepository.findById(anyString())).thenReturn(Optional.empty());

    BusinessException ex = assertThrows(BusinessException.class, () -> {
      employeeService.updateLeaveRequestStatus("1", 1L, review);
    });
    assertEquals(ex.getError().getErrorDescription().getErrorCode(),
        BusinessErrorCode.EMPLOYEE_NOT_FOUND.getErrorCode());
    assertEquals(ex.getError().getErrorDescription().getDevMsg(),
        BusinessErrorCode.EMPLOYEE_NOT_FOUND.getDevMsg());
    assertEquals(ex.getError().getErrorDescription().getStatus(), HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateLeaveRequestStatusRequestNotFound() {
    EmployeeEty employee = new EmployeeEty();
    LeaveRequestReview answer = new LeaveRequestReview();

    when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
    when(leaveRequestRepository.findById(1L)).thenReturn(Optional.empty());

    BusinessException ex = assertThrows(BusinessException.class, () -> {
      employeeService.updateLeaveRequestStatus("1", 1L, answer);
    });
    assertEquals(ex.getError().getErrorDescription().getErrorCode(),
        BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND.getErrorCode());
    assertEquals(ex.getError().getErrorDescription().getStatus(), HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateLeaveRequestStatusAlreadyAnswered() {
    EmployeeEty employee = new EmployeeEty();
    LeaveRequestEty request = new LeaveRequestEty();
    request.setStatus(LeaveRequestStatus.APPROVED);
    request.setV(1L);

    LeaveRequestReview review = new LeaveRequestReview();
    review.setV(1L);
    review.setType("APPROVED");
    when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
    when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));

    BusinessException ex = assertThrows(BusinessException.class, () -> {
      employeeService.updateLeaveRequestStatus("1", 1L, review);
    });
    assertEquals(BusinessErrorCode.LEAVE_REQUEST_NOT_PENDING.getErrorCode(),
        ex.getError().getErrorDescription().getErrorCode());
    assertEquals(BusinessErrorCode.LEAVE_REQUEST_NOT_PENDING.getDevMsg(),
        ex.getError().getErrorDescription().getDevMsg());
    assertEquals(HttpStatus.BAD_REQUEST, ex.getError().getErrorDescription().getStatus());
  }

  @Test
  void updateLeaveRequestStatusOutdatedVersion() {
    EmployeeEty employee = new EmployeeEty();
    LeaveRequestEty request = new LeaveRequestEty();
    request.setV(2L);

    LeaveRequestReview answer = new LeaveRequestReview();
    answer.setV(1L);

    when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
    when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));

    BusinessException ex = assertThrows(BusinessException.class, () -> {
      employeeService.updateLeaveRequestStatus("1", 1L, answer);
    });

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_VERSION_CONFLICT.getErrorCode(),
        ex.getError().getErrorDescription().getErrorCode());
    assertEquals(BusinessErrorCode.LEAVE_REQUEST_VERSION_CONFLICT.getDevMsg(),
        ex.getError().getErrorDescription().getDevMsg());
    assertEquals(ex.getError().getErrorDescription().getStatus(), HttpStatus.CONFLICT);
  }

  @Test
  void acceptLeaveRequest() {
    EmployeeEty employee = new EmployeeEty();
    LeaveRequestEty request = new LeaveRequestEty();
    request.setStatus(LeaveRequestStatus.PENDING);
    request.setV(1L);

    LeaveRequestReview review = new LeaveRequestReview();
    review.setV(1L);
    review.setType("APPROVAL");

    when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
    when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));

    LeaveRequestEty updatedRequest = employeeService.updateLeaveRequestStatus("1", 1L, review);
    assertEquals(updatedRequest.getStatus(), LeaveRequestStatus.APPROVED);
    assertNull(updatedRequest.getRejectReason());
    verify(leaveRequestRepository, times(1)).save(any());
  }

  @Test
  void rejectLeaveRequest() {
    EmployeeEty employee = new EmployeeEty();
    LeaveRequestEty request = new LeaveRequestEty();
    request.setStatus(LeaveRequestStatus.PENDING);
    request.setV(1L);

    LeaveRequestReview review = new LeaveRequestReview();
    review.setV(2L);
    review.setType("REJECTION");
    review.setRejectionReason("Not a good time");
    when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
    when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));

    LeaveRequestEty updatedRequest = employeeService.updateLeaveRequestStatus("1", 1L, review);
    assertEquals(updatedRequest.getStatus(), LeaveRequestStatus.REJECTED);
    assertEquals(updatedRequest.getRejectReason(), "Not a good time");
  }

  @Test
  void inactivateEmployeeSuccess() {
    EmployeeEty employee = initEmployee();

    when(employeeRepository.findById(ID)).thenReturn(Optional.of(employee));
    when(tokenUtil.getLoggedUserId()).thenReturn("mdf_usr");

    employeeService.inactivateEmployee(ID);

    assertEquals("INACTIVE", employee.getStatus());
    assertEquals("mdf_usr", employee.getMdfUsr());
  }

  @Test
  void inactivateEmployeeFail() {

    when(employeeRepository.findById(ID)).thenReturn(Optional.empty());

    BusinessException exception = assertThrows(BusinessException.class,
        () -> employeeService.inactivateEmployee(ID));

    assertEquals(BusinessErrorCode.EMPLOYEE_NOT_FOUND, exception.getError().getErrorDescription());
    verify(employeeRepository, never()).save(any());
  }

  @Test
  void getEmployeeRemainingDaysOffIdNotFound() {
    when(employeeRepository.findById(anyString())).thenReturn(Optional.empty());
    var ex = assertThrows(BusinessException.class, () -> {
      employeeService.getEmployeeRemainingDaysOff(ID);
    });
    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.EMPLOYEE_NOT_FOUND);
  }

  @Test
  void getEmployeeRemainingDaysOffNotSet() {
    EmployeeEty employee = new EmployeeEty();
    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    var ex = assertThrows(BusinessException.class, () -> {
      employeeService.getEmployeeRemainingDaysOff(ID);
    });
    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.YEARLY_DAYS_OFF_NOT_SET);
  }

  @Test
  void getEmployeeRemainingDaysOff() {
    EmployeeEty employee = new EmployeeEty();
    EmpYearlyDaysOffEty daysOff = new EmpYearlyDaysOffEty();
    daysOff.setTotalNoDays(20);
    daysOff.setYear(2023);
    employee.setEmpYearlyDaysOff(Collections.singleton(daysOff));

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));
    RemainingDaysOff remainingDaysOff = employeeService.getEmployeeRemainingDaysOff(ID);
    assertEquals(20, remainingDaysOff.getRemainingDays());
  }

  @Test
  void checkEmployeeUniqueCredentialsDuplicateUsername() {
    when(employeeRepository.existsByUsername(USERNAME)).thenReturn(true);
    var ex = assertThrows(BusinessException.class, () ->
        employeeService.checkEmployeeUniqueCredentials(USERNAME, "unique@gmail.com"));
    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.USERNAME_DUPLICATE);
  }

  @Test
  void checkEmployeeUniqueCredentialsDuplicateEmail() {
    when(employeeRepository.existsByUsername("unique.name")).thenReturn(false);
    when(employeeRepository.existsByEmail(EMAIL)).thenReturn(true);
    var ex = assertThrows(BusinessException.class, () -> {
      employeeService.checkEmployeeUniqueCredentials("unique.name", EMAIL);
    });
    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.EMAIL_DUPLICATE);
  }

  @Test
  void checkEmployeeUniqueCredentials() {
    when(employeeRepository.existsByUsername("unique.name")).thenReturn(false);
    when(employeeRepository.existsByEmail("unique@gmail.com")).thenReturn(false);
    employeeService.checkEmployeeUniqueCredentials("unique.name", "unique@gmail.com");
    verify(employeeRepository, times(1)).existsByUsername(anyString());
    verify(employeeRepository, times(1)).existsByEmail(anyString());
  }

  @Test
  void editLeaveRequestNotFound() {
    EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
    EmployeeEty employee = new EmployeeEty();

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> employeeService.editLeaveRequest(ID, 1L, leaveRequestEdit));

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND,
        exception.getError().getErrorDescription());
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  void editLeaveRequestPrecedingVersion() {

    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    Long leaveRequestIdValue = 1L;
    leaveRequest.setId(leaveRequestIdValue);
    leaveRequest.setV(2L);

    EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
    leaveRequestEdit.setV(1L);

    EmployeeEty employee = new EmployeeEty();
    employee.getLeaveRequests().add(leaveRequest);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    BusinessException exception = assertThrows(BusinessException.class,
            () -> employeeService.editLeaveRequest(ID, leaveRequestIdValue, leaveRequestEdit));

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_VERSION_CONFLICT,
            exception.getError().getErrorDescription());
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  void editLeaveRequestRejected() {
    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    Long leaveRequestIdValue = 1L;
    leaveRequest.setId(leaveRequestIdValue);
    leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
    leaveRequest.setV(1L);

    EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
    leaveRequestEdit.setV(2L);

    EmployeeEty employee = new EmployeeEty();
    employee.getLeaveRequests().add(leaveRequest);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    BusinessException exception = assertThrows(BusinessException.class,
            () -> employeeService.editLeaveRequest(ID, leaveRequestIdValue, leaveRequestEdit));

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_UPDATE_ALREADY_REJECTED,
            exception.getError().getErrorDescription());
    verify(employeeRepository, times(1)).findById(anyString());
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  void editLeaveRequestPastDate() {
    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    Long leaveRequestIdValue = 1L;
    leaveRequest.setId(leaveRequestIdValue);
    leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
    leaveRequest.setV(1L);

    EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
    leaveRequestEdit.setStartDate(LocalDate.parse("2023-07-25"));
    leaveRequestEdit.setV(2L);

    EmployeeEty employee = new EmployeeEty();
    employee.setId(ID);
    employee.getLeaveRequests().add(leaveRequest);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> employeeService.editLeaveRequest(ID, leaveRequestIdValue, leaveRequestEdit));

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_UPDATE_IN_PAST,
        exception.getError().getErrorDescription());

    verify(employeeRepository, times(1)).findById(anyString());
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  void editLeaveRequestSuccess() {
    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    Long leaveRequestIdValue = 1L;
    leaveRequest.setId(leaveRequestIdValue);
    leaveRequest.setType(LeaveRequestType.MEDICAL);
    leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
    leaveRequest.setStartDate(LocalDate.parse("2023-08-23"));
    leaveRequest.setEndDate(LocalDate.parse("2023-08-27"));
    leaveRequest.setDescription("Medical leave request");
    leaveRequest.setV(1L);

    EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
    leaveRequestEdit.setStartDate(LocalDate.parse("2023-08-25"));
    leaveRequestEdit.setEndDate(LocalDate.parse("2023-08-28"));
    leaveRequestEdit.setType(LeaveRequestType.VACATION);
    leaveRequestEdit.setDescription("Vacation leave request");
    leaveRequestEdit.setV(1L);

    EmployeeEty employee = new EmployeeEty();
    employee.setId(ID);
    employee.getLeaveRequests().add(leaveRequest);
    Set<EmpYearlyDaysOffEty> daysOffEtySet = new HashSet<>();
    EmpYearlyDaysOffEty daysOff = new EmpYearlyDaysOffEty();
    daysOff.setEmployee(employee);
    daysOff.setYear(2023);
    daysOff.setTotalNoDays(30);
    daysOffEtySet.add(daysOff);
    employee.setEmpYearlyDaysOff(daysOffEtySet);

    LeaveRequestEty savedLeaveRequest = new LeaveRequestEty();
    savedLeaveRequest.setId(leaveRequestIdValue);
    savedLeaveRequest.setType(LeaveRequestType.VACATION);
    savedLeaveRequest.setStartDate(LocalDate.parse("2023-08-25"));
    savedLeaveRequest.setEndDate(LocalDate.parse("2023-08-28"));
    savedLeaveRequest.setDescription("Vacation leave request");
    savedLeaveRequest.setStatus(LeaveRequestStatus.PENDING);
    savedLeaveRequest.setV(1L);

    LegallyDaysOffList daysOffList = new LegallyDaysOffList();
    daysOffList.setDays(new ArrayList<>());

    when(legallyDaysOffService.getAllLegallyOffDays()).thenReturn(daysOffList);
    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));
    when(leaveRequestRepository.save(any())).thenReturn(savedLeaveRequest);

    LeaveRequestDetailsListItem leaveRequestItem = employeeService.editLeaveRequest(ID,
        leaveRequestIdValue,
        leaveRequestEdit);

    assertEquals(leaveRequestEdit.getStartDate(), leaveRequestItem.getStartDate());
    assertEquals(leaveRequestEdit.getEndDate(), leaveRequestItem.getEndDate());
    assertEquals(leaveRequestEdit.getType(), leaveRequestItem.getType());
    assertEquals(leaveRequestEdit.getDescription(), leaveRequestItem.getDescription());
    assertEquals(LeaveRequestStatus.PENDING, leaveRequestItem.getStatus());
    assertEquals(LeaveRequestStatus.PENDING, leaveRequestItem.getStatus());

    verify(employeeRepository, times(1)).findById(anyString());
    verify(leaveRequestRepository, times(1)).save(any(LeaveRequestEty.class));
  }

  @Test
  public void deleteRejectedLeaveRequest() {
    String employeeId = ID;
    Long requestId = 1L;

    EmployeeEty employee = initEmployee();

    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    leaveRequest.setId(requestId);
    leaveRequest.setType(LeaveRequestType.MEDICAL);
    leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
    leaveRequest.setStartDate(LocalDate.parse("2023-08-23"));
    leaveRequest.setEndDate(LocalDate.parse("2023-08-27"));
    leaveRequest.setDescription("Medical leave request");

    leaveRequest.setEmployee(employee);
    employee.getLeaveRequests().add(leaveRequest);

    Optional<EmployeeEty> employeeEtyOptional = Optional.of(employee);

    when(employeeRepository.findById(anyString())).thenReturn(employeeEtyOptional);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> employeeService.deleteLeaveRequest(employeeId, requestId));

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_DELETE_ALREADY_REJECTED,
        exception.getError().getErrorDescription());

    assertEquals(1, employeeEtyOptional.get().getLeaveRequests().size());
    verify(employeeRepository, times(1)).findById(anyString());
    verify(employeeRepository, times(0)).save(any(EmployeeEty.class));
  }

  @Test
  public void deleteApprovedPastLeaveRequest() {
    String employeeId = ID;
    Long requestId = 1L;

    EmployeeEty employee = initEmployee();

    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    leaveRequest.setId(requestId);
    leaveRequest.setType(LeaveRequestType.MEDICAL);
    leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
    leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
    leaveRequest.setStartDate(LocalDate.parse("2023-07-23"));
    leaveRequest.setEmployee(employee);
    employee.getLeaveRequests().add(leaveRequest);

    Optional<EmployeeEty> employeeEtyOptional = Optional.of(employee);

    when(employeeRepository.findById(anyString())).thenReturn(employeeEtyOptional);

    BusinessException exception = assertThrows(BusinessException.class,
        () -> employeeService.deleteLeaveRequest(employeeId, requestId));

    assertEquals(BusinessErrorCode.LEAVE_REQUEST_DELETE_APPROVED_PAST_DATE,
        exception.getError().getErrorDescription());

    assertEquals(1, employeeEtyOptional.get().getLeaveRequests().size());
    verify(employeeRepository, times(1)).findById(anyString());
    verify(employeeRepository, times(0)).save(any(EmployeeEty.class));
  }

  @Test
  public void deleteLeaveRequestSuccess() {
    EmployeeEty employee = initEmployee();

    LeaveRequestEty leaveRequest = new LeaveRequestEty();
    leaveRequest.setId(1L);
    leaveRequest.setType(LeaveRequestType.MEDICAL);
    leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
    leaveRequest.setStartDate(LocalDate.parse("2023-08-23"));
    leaveRequest.setEndDate(LocalDate.parse("2023-08-27"));
    leaveRequest.setDescription("Medical leave request");

    leaveRequest.setEmployee(employee);
    employee.getLeaveRequests().add(leaveRequest);

    Optional<EmployeeEty> employeeEtyOptional = Optional.of(employee);

    when(employeeRepository.findById(anyString())).thenReturn(employeeEtyOptional);

    employeeService.deleteLeaveRequest(ID, 1L);

    assertEquals(0, employeeEtyOptional.get().getLeaveRequests().size());
    verify(employeeRepository, times(1)).findById(anyString());
    verify(employeeRepository, times(1)).save(any(EmployeeEty.class));
  }


  private EmployeeEty initEmployee() {

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
  @DisplayName("Test the EMPLOYEE_NOT_FOUND exception is thrown")
  void testCreateLeaveRequestInvalidEmployee() {
    when(employeeRepository.findById(anyString())).thenReturn(Optional.empty());

    CreateLeaveRequestDetails leaveRequestDetails = new CreateLeaveRequestDetails();
    assertThrows(BusinessException.class,
        () -> employeeService.createLeaveRequest("1", leaveRequestDetails));

    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  @DisplayName("Test the LEAVE_RQST_INVALID_PERIOD exception is thrown")
  void testCreateLeaveRequestInvalidPeriod() {
    EmployeeEty employee = new EmployeeEty();
    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    CreateLeaveRequestDetails leaveRequestDetails = new CreateLeaveRequestDetails();
    leaveRequestDetails.setStartDate(LocalDate.ofInstant(clock.instant(), clock.getZone()));
    leaveRequestDetails.setEndDate(LocalDate.ofInstant(clock.instant(), clock.getZone()).minusDays(1));

    BusinessException ex = assertThrows(BusinessException.class,
        () -> employeeService.createLeaveRequest("1", leaveRequestDetails));

    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.LEAVE_RQST_INVALID_PERIOD);
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  @DisplayName("Test the LEAVE_RQST_DIFF_YEARS exception is thrown")
  void testCreateLeaveRequestDifferentYears() {
    EmployeeEty employee = new EmployeeEty();
    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    CreateLeaveRequestDetails leaveRequestDetails = new CreateLeaveRequestDetails();
    leaveRequestDetails.setStartDate(LocalDate.of(2023, 1, 1));
    leaveRequestDetails.setEndDate(LocalDate.of(2024, 1, 1));

    BusinessException ex = assertThrows(BusinessException.class,
        () -> employeeService.createLeaveRequest("1", leaveRequestDetails));

    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.LEAVE_RQST_DIFF_YEARS);
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  @DisplayName("Test the LEAVE_RQST_INVALID_PERIOD exception is thrown")
  void testCreateLeaveRequestInvalidMonth() {
    EmployeeEty employee = new EmployeeEty();
    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

    CreateLeaveRequestDetails leaveRequestDetails = new CreateLeaveRequestDetails();
    leaveRequestDetails.setStartDate(LocalDate.ofInstant(clock.instant(), clock.getZone()).minusMonths(1));
    leaveRequestDetails.setEndDate(LocalDate.ofInstant(clock.instant(), clock.getZone()));

    BusinessException ex = assertThrows(BusinessException.class,
        () -> employeeService.createLeaveRequest("1", leaveRequestDetails));

    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.LEAVE_RQST_INVALID_MONTH);
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  @DisplayName("Test the LEAVE_RQST_INVALID_NUMBER_DAYS exception is thrown")
  void testCreateLeaveRequestInvalidNumberOfDays () {
    EmployeeEty employee = new EmployeeEty();
    LegallyDaysOffList daysOffList = new LegallyDaysOffList();
    daysOffList.setDays(new ArrayList<>());

    EmpYearlyDaysOffEty daysOffEty = new EmpYearlyDaysOffEty();
    daysOffEty.setYear(2023);
    daysOffEty.setTotalNoDays(4);
    employee.setEmpYearlyDaysOff(Collections.singleton(daysOffEty));

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));
    when(legallyDaysOffService.getAllLegallyOffDays()).thenReturn(daysOffList);
    CreateLeaveRequestDetails leaveRequestDetails = new CreateLeaveRequestDetails();
    leaveRequestDetails.setStartDate(LocalDate.of(2023, 8, 21));
    leaveRequestDetails.setEndDate(LocalDate.of(2023, 8, 25));

    BusinessException ex = assertThrows(BusinessException.class,
            () -> employeeService.createLeaveRequest("1", leaveRequestDetails));

    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.LEAVE_RQST_INVALID_NUMBER_DAYS);
    verify(leaveRequestRepository, never()).save(any());
  }

  @Test
  @DisplayName("Test the creation of a leave request")
  void testCreateLeaveRequestSuccess () {
    EmployeeEty employee = new EmployeeEty();
    LegallyDaysOffList daysOffList = new LegallyDaysOffList();
    daysOffList.setDays(new ArrayList<>());

    EmpYearlyDaysOffEty daysOffEty = new EmpYearlyDaysOffEty();
    daysOffEty.setYear(2023);
    daysOffEty.setTotalNoDays(5);
    Set<EmpYearlyDaysOffEty> daysOffSet = new HashSet<>();
    daysOffSet.add(daysOffEty);
    employee.setEmpYearlyDaysOff(daysOffSet);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));
    when(legallyDaysOffService.getAllLegallyOffDays()).thenReturn(daysOffList);
    CreateLeaveRequestDetails leaveRequestDetails = new CreateLeaveRequestDetails();
    leaveRequestDetails.setStartDate(LocalDate.of(2023, 8, 21));
    leaveRequestDetails.setEndDate(LocalDate.of(2023, 8, 25));

    employeeService.createLeaveRequest("1", leaveRequestDetails);
    verify(employeeRepository, times(1)).save(any());
  }

  @Test
  public void getLeaveRequestsEmployeeNotFound () {
    when(employeeRepository.findById(anyString())).thenThrow(
        new BusinessException(
            BusinessException.BusinessExceptionElement
                .builder()
                .errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND)
                .build()));

    BusinessException ex = assertThrows(BusinessException.class, () -> {
      employeeService.getLeaveRequests("1", LocalDate.of(2023, 8, 1),
          LocalDate.of(2023, 8, 10));
    });
    assertEquals(ex.getError().getErrorDescription().getStatus(), HttpStatus.BAD_REQUEST);
    assertEquals(ex.getError().getErrorDescription().getDevMsg(),
        BusinessErrorCode.EMPLOYEE_NOT_FOUND.getDevMsg());
    assertEquals(ex.getError().getErrorDescription().getErrorCode(),
        BusinessErrorCode.EMPLOYEE_NOT_FOUND.getErrorCode());
  }

  @Test
  public void getLeaveRequestsNoDates() {
    EmployeeEty employee = new EmployeeEty();
    employee.setId("1");
    LeaveRequestEty request1 = new LeaveRequestEty();
    LeaveRequestEty request2 = new LeaveRequestEty();
    request1.setId(1L);
    request1.setEmployee(employee);
    request2.setId(2L);
    request2.setEmployee(employee);
    Set<LeaveRequestEty> requestsSet = new HashSet<>();
    requestsSet.add(request1);
    requestsSet.add(request2);
    employee.setLeaveRequests(requestsSet);
    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));
    LeaveRequestDetailsList requests = employeeService.getLeaveRequests("1", null, null);
    assertEquals(requests.getItems().size(), 2);
    assertEquals(requests.getItems().get(0).getEmployeeDetails().getEmployeeId(), "1");
    assertEquals(requests.getItems().get(0).getId(), 1L);
    assertEquals(requests.getItems().get(1).getEmployeeDetails().getEmployeeId(), "1");
    assertEquals(requests.getItems().get(1).getId(), 2L);
  }

  @Test
  public void getLeaveRequests() {
    EmployeeEty employee = new EmployeeEty();
    employee.setId("1");
    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));
    LeaveRequestEty request1 = new LeaveRequestEty();
    LeaveRequestEty request2 = new LeaveRequestEty();
    LeaveRequestEty request3 = new LeaveRequestEty();

    request1.setId(1L);
    request1.setStartDate(LocalDate.of(2023, 8, 1));
    request1.setEndDate(LocalDate.of(2023, 8, 5));
    request1.setEmployee(employee);

    request2.setId(2L);
    request2.setStartDate(LocalDate.of(2023, 8, 7));
    request2.setEndDate(LocalDate.of(2023, 8, 10));
    request2.setEmployee(employee);

    request3.setId(3L);
    request3.setStartDate(LocalDate.of(2023, 8, 3));
    request3.setEndDate(LocalDate.of(2023, 8, 11));
    request3.setEmployee(employee);
    Set<LeaveRequestEty> requestsSet = new HashSet<>();
    requestsSet.add(request1);
    requestsSet.add(request2);
    requestsSet.add(request3);
    employee.setLeaveRequests(requestsSet);

    String id = "1";
    LocalDate startDate = LocalDate.of(2023, 8, 1);
    LocalDate endDate = LocalDate.of(2023, 8, 10);
    LeaveRequestDetailsList requests = employeeService.getLeaveRequests(id, startDate, endDate);
    assertEquals(requests.getItems().size(), 2);
    assertEquals(requests.getItems().get(0).getEmployeeDetails().getEmployeeId(), "1");
    assertEquals(requests.getItems().get(0).getId(), 1L);
    assertEquals(requests.getItems().get(1).getEmployeeDetails().getEmployeeId(), "1");
    assertEquals(requests.getItems().get(1).getId(), 2L);
  }

  @Test
  void changeVacationDays() {
    EmpYearlyDaysOffEty yearlyDaysOff1 = new EmpYearlyDaysOffEty();
    EmpYearlyDaysOffEty yearlyDaysOff2 = new EmpYearlyDaysOffEty();

    EmployeeEty employee = new EmployeeEty();
    employee.setId("id1M");
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

    EmployeeEty employee2 = new EmployeeEty();
    employee2.setId("id2M");
    employee2.setFirstName(FIRST_NAME);
    employee2.setLastName(LAST_NAME);
    employee2.setEmail("alt-mail");
    employee2.setCrtUsr(CRT_USR);
    employee2.setCrtTms(CRT_TMS);
    employee2.setMdfUsr(MDF_USR);
    employee2.setMdfTms(MDF_TMS);
    employee2.setRole(ROLE);
    employee2.setStatus(STATUS);
    employee2.setContractStartDate(CONTRACT_START_DATE);
    employee2.setContractEndDate(CONTRACT_END_DATE);
    employee2.setUsername("utilizator");
    employee2.setTeam(TEAM_ETY);

    yearlyDaysOff1.setTotalNoDays(15);
    yearlyDaysOff1.setYear(2023);
    yearlyDaysOff1.setEmployee(employee);
    yearlyDaysOff1.setId(1L);

    Set<EmpYearlyDaysOffEty> set1 = new HashSet<>();
    set1.add(yearlyDaysOff1);
    employee.setEmpYearlyDaysOff(set1);

    yearlyDaysOff2.setTotalNoDays(25);
    yearlyDaysOff2.setYear(2023);
    yearlyDaysOff2.setEmployee(employee2);
    yearlyDaysOff2.setId(2L);

    Set<EmpYearlyDaysOffEty> set2 = new HashSet<>();
    set2.add(yearlyDaysOff2);
    employee2.setEmpYearlyDaysOff(set2);

    List<EmployeeEty> lista = new ArrayList<>();
    lista.add(employee);
    lista.add(employee2);
    when(employeeRepository.findAllById(any())).thenReturn(lista);
    when(tokenUtil.getLoggedUserId()).thenReturn("mdf_usr");

    VacationDaysModifyDetails v = new VacationDaysModifyDetails();

    v.setNoDays(1);
    v.setType(VacationDaysChangeType.INCREASE);
    v.setDescription("d");
    List<String> idList = new ArrayList<>();
    idList.add("id1M");
    idList.add("id2M");
    v.setEmployeeIds(idList);

    employeeService.changeVacationDays(v);

    verify(employeeRepository, times(2))
        .save(any());

  }

  @Test
  void changeVacationDaysNegativeDays() {

    EmpYearlyDaysOffEty yearlyDaysOff2 = new EmpYearlyDaysOffEty();

    EmployeeEty employee2 = new EmployeeEty();
    employee2.setId("id2M");
    employee2.setFirstName(FIRST_NAME);
    employee2.setLastName(LAST_NAME);
    employee2.setEmail("alt-mail");
    employee2.setCrtUsr(CRT_USR);
    employee2.setCrtTms(CRT_TMS);
    employee2.setMdfUsr(MDF_USR);
    employee2.setMdfTms(MDF_TMS);
    employee2.setRole(ROLE);
    employee2.setStatus(STATUS);
    employee2.setContractStartDate(CONTRACT_START_DATE);
    employee2.setContractEndDate(CONTRACT_END_DATE);
    employee2.setUsername("utilizator");
    employee2.setTeam(TEAM_ETY);

    yearlyDaysOff2.setTotalNoDays(25);
    yearlyDaysOff2.setYear(2023);
    yearlyDaysOff2.setEmployee(employee2);
    yearlyDaysOff2.setId(2L);

    Set<EmpYearlyDaysOffEty> set2 = new HashSet<>();
    set2.add(yearlyDaysOff2);
    employee2.setEmpYearlyDaysOff(set2);

    List<EmployeeEty> lista = new ArrayList<>();
    lista.add(employee2);
    when(employeeRepository.findAllById(any())).thenReturn(lista);

    VacationDaysModifyDetails v = new VacationDaysModifyDetails();

    v.setNoDays(111);
    v.setType(VacationDaysChangeType.DECREASE);
    v.setDescription("d");
    List<String> idList = new ArrayList<>();
    idList.add("id2M");
    v.setEmployeeIds(idList);

    var ex = assertThrows(BusinessException.class, () -> {
      employeeService.changeVacationDays(v);
    });
    assertEquals(ex.getError().getErrorDescription(), BusinessErrorCode.NEGATIVE_DAYS_OFF);

  }

  @Test
  void createEmployee() {

    TeamEty team = new TeamEty();
    team.setId(1L);
    team.setName("Backend");
    team.setCrtUsr("crtUsr");
    team.setCrtTms(clock.instant());
    team.setMdfUsr("mdfUsr");
    team.setMdfTms(clock.instant());
    team.setStatus(TeamStatus.ACTIVE);

    EmployeeEty employee = new EmployeeEty(
        "11",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        team,
        new HashSet<>(),
        new HashSet<>()
    );

    when(teamRepository.findById(any())).thenReturn(Optional.of(team));
    //when(employeeRepository.findEmployeeByUsername(employee.getUsername())).thenReturn(Optional.empty());
    when(employeeRepository.save(any())).thenReturn(employee);

    RegisterRequest request = new RegisterRequest();
    request.setFirstname("jon");
    request.setLastname("doe");
    request.setUsername("jon121");
    request.setTeamId(1L);
    request.setRole("USER");
    request.setEmail("jon@mail.com");
    request.setContractStartDate(LocalDate.ofInstant(clock.instant(), clock.getZone()));
    request.setNoDaysOff(20);

    UserDetailsResponse userDetails = new UserDetailsResponse();
    userDetails.setEmployeeId("11-hr-2323");
    userDetails.setUsername("user_hr122");
    userDetails.setRoles(Collections.singletonList("HR"));
    userDetails.setTeamDetails(new TeamDetails(3L, "HR"));

    EmployeeDetailsListItem returnedEmployee = employeeService.createEmployee(request,
        "hr_user_id");

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
    team.setCrtTms(clock.instant());
    team.setMdfUsr("mdfUsr");
    team.setMdfTms(clock.instant());
    team.setStatus(TeamStatus.ACTIVE);

    EmployeeEty employee = new EmployeeEty(
        "12",
        "jon",
        "doe",
        "email@bla.com",
        "crtUsr",
        clock.instant(),
        "mdfUsr",
        clock.instant(),
        "role.user",
        "status.active",
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        LocalDate.ofInstant(clock.instant(), clock.getZone()),
        "jon121",
        passwordEncoder.encode("axon_jon121"),
        team,
        new HashSet<>(),
        new HashSet<>()
    );

    when(employeeRepository.findEmployeeByUsername(any())).thenReturn(Optional.of(employee));

    EmployeeEty loadedEmployee = employeeService.loadEmployeeByUsername("test.user2");

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

  @Test
  void updateEmployeeDetails_Success() {

    EmployeeEty existingEmployee = new EmployeeEty();
    existingEmployee.setId("1");
    existingEmployee.setV(1L);

    EmployeeUpdateRequest updatedEmployeeUpdateRequest = new EmployeeUpdateRequest();
    updatedEmployeeUpdateRequest.setTeamId("1");
    updatedEmployeeUpdateRequest.setFirstName("Updated First Name");
    updatedEmployeeUpdateRequest.setLastName("Updated Last Name");
    updatedEmployeeUpdateRequest.setEmail("updated@axonsoft.com");
    updatedEmployeeUpdateRequest.setRole("USER");
    updatedEmployeeUpdateRequest.setV(2L);

    TeamEty team = new TeamEty();
    when(teamRepository.findById(anyLong())).thenReturn(Optional.of(team));
    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(existingEmployee));
    employeeService.updateEmployeeDetails("1", updatedEmployeeUpdateRequest);
    verify(employeeRepository).save(existingEmployee);
  }

  @Test
  void updateEmployeeDetails_Conflict() {

    EmployeeEty existingEmployee = new EmployeeEty();
    existingEmployee.setId("1");
    existingEmployee.setV(2L);

    EmployeeUpdateRequest updatedEmployeeUpdateRequest = new EmployeeUpdateRequest();
    updatedEmployeeUpdateRequest.setTeamId("1");
    updatedEmployeeUpdateRequest.setFirstName("Updated First Name");
    updatedEmployeeUpdateRequest.setLastName("Updated Last Name");
    updatedEmployeeUpdateRequest.setEmail("updated@axonsoft.com");
    updatedEmployeeUpdateRequest.setRole("USER");
    updatedEmployeeUpdateRequest.setV(1L);

    when(employeeRepository.findById(anyString())).thenReturn(Optional.of(existingEmployee));

    assertThrows(BusinessException.class,
        () -> employeeService.updateEmployeeDetails("1", updatedEmployeeUpdateRequest),
        "Expected BusinessException with CONFLICT error code");

    verify(employeeRepository, never()).save(existingEmployee);
  }

}