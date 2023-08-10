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
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.LeaveRequestEty;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;
import ro.axon.dot.domain.LeaveRequestRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsListItem;
import ro.axon.dot.model.RemainingDaysOff;
import ro.axon.dot.model.LeaveRequestReview;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    EmployeeService employeeService;

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    LeaveRequestRepository leaveRequestRepository;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, leaveRequestRepository);

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
  void updateLeaveRequestStatusEmployeeNotFound() {
      LeaveRequestReview review = new LeaveRequestReview();
      review.setLeaveRequestStatus("APPROVED");
      review.setVersion(1L);
      when(employeeRepository.findById(anyString())).thenReturn(Optional.empty());

      var ex = assertThrows(BusinessException.class, () -> {
          employeeService.updateLeaveRequestStatus(1L, 1L, review);
      });
      assertEquals(ex.getError().getErrorDescription().getErrorCode(),"EDOT0001400");
      assertEquals(ex.getError().getErrorDescription().getDevMsg(),"The employee with the given ID does not exist.");
      assertEquals(ex.getError().getErrorDescription().getStatus(), HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateLeaveRequestStatusRequestNotFound() {
      EmployeeEty employee = new EmployeeEty();
      LeaveRequestReview answer = new LeaveRequestReview();

      when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
      when(leaveRequestRepository.findById(1L)).thenReturn(Optional.empty());


      var ex = assertThrows(BusinessException.class, () -> {
                  employeeService.updateLeaveRequestStatus(1L, 1L, answer);
              });
      assertEquals(ex.getError().getErrorDescription().getErrorCode(),"EDOT0003400");
      assertEquals(ex.getError().getErrorDescription().getDevMsg(),"Request not found");
      assertEquals(ex.getError().getErrorDescription().getStatus(), HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateLeaveRequestStatusAlreadyAnswered() {
      EmployeeEty employee = new EmployeeEty();
      LeaveRequestEty request = new LeaveRequestEty();
      request.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
      request.setV(1L);

      LeaveRequestReview review = new LeaveRequestReview();
      review.setVersion(1L);
      review.setLeaveRequestStatus("APPROVED");
      when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
      when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));

      var ex = assertThrows(BusinessException.class, () -> {
          employeeService.updateLeaveRequestStatus(1L, 1L, review);
      });
      assertEquals(ex.getError().getErrorDescription().getErrorCode(),"EDOT0004400");
      assertEquals(ex.getError().getErrorDescription().getDevMsg(),"Request already answered");
      assertEquals(ex.getError().getErrorDescription().getStatus(), HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateLeaveRequestStatusOutdatedVersion() {
      EmployeeEty employee = new EmployeeEty();
      LeaveRequestEty request = new LeaveRequestEty();
      request.setV(2L);

      LeaveRequestReview answer = new LeaveRequestReview();
      answer.setVersion(1L);

      when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
      when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));


      var ex = assertThrows(BusinessException.class, () -> {
          employeeService.updateLeaveRequestStatus(1L, 1L, answer);
        });

      assertEquals(ex.getError().getErrorDescription().getErrorCode(),"EDOT0005400");
      assertEquals(ex.getError().getErrorDescription().getDevMsg(),"Request version smaller than db version");
      assertEquals(ex.getError().getErrorDescription().getStatus(), HttpStatus.CONFLICT);
  }

  @Test
  void updateLeaveRequestStatus() {
      EmployeeEty employee = new EmployeeEty();
      LeaveRequestEty request = new LeaveRequestEty();
      request.setStatus(LeaveRequestEtyStatusEnum.PENDING);
      request.setV(1L);

      LeaveRequestReview answer = new LeaveRequestReview();
      answer.setVersion(1L);
      answer.setLeaveRequestStatus("APPROVED");

      when(employeeRepository.findById("1")).thenReturn(Optional.of(employee));
      when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));
      // accept
      request.setV(1L);
      LeaveRequestEty updatedRequest = employeeService.updateLeaveRequestStatus(1L, 1L, answer);
      assertEquals(updatedRequest.getStatus(), LeaveRequestEtyStatusEnum.APPROVED);
      assertEquals(updatedRequest.getV(), 1L);
      assertNull(updatedRequest.getRejectReason());

      // reject
      request.setStatus(LeaveRequestEtyStatusEnum.PENDING);
      answer.setLeaveRequestStatus("REJECTED");
      answer.setRejectReason("Not a good time");
      answer.setVersion(3L);
      updatedRequest = employeeService.updateLeaveRequestStatus(1L, 1L, answer);
      assertEquals(updatedRequest.getStatus(), LeaveRequestEtyStatusEnum.REJECTED);
      assertEquals(updatedRequest.getRejectReason(), "Not a good time");
      assertEquals(updatedRequest.getV(), 3L);

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
    public void getLeaveRequestsEmployeeNotFound() {
        when(employeeRepository.findById(anyString())).thenThrow(
                new BusinessException(
                        BusinessException.BusinessExceptionElement
                                .builder()
                                .errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND)
                                .build()));

        var ex = assertThrows(BusinessException.class, () -> {
            employeeService.getLeaveRequests("1", LocalDate.of(2023, 8, 1),
                    LocalDate.of(2023, 8, 10));
        });
        assertEquals(ex.getError().getErrorDescription().getStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(ex.getError().getErrorDescription().getDevMsg(), "Employee not found");
        assertEquals(ex.getError().getErrorDescription().getErrorCode(), "EDOT0001400");
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
        employee.setLeaveRequests(Arrays.asList(request1, request2));
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
        employee.setLeaveRequests(Arrays.asList(request1, request2, request3));


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
}