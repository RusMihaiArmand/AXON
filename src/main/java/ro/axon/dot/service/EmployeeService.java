package ro.axon.dot.service;

import java.time.LocalDate;
import java.util.Calendar;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.LeaveRequestEty;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;
import ro.axon.dot.domain.LeaveRequestRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.mapper.LeaveRequestMapper;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsListItem;
import ro.axon.dot.model.RemainingDaysOff;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final LeaveRequestRepository leaveRequestRepository;

  public EmployeeDetailsList getEmployeesDetails(String name) {
    var employeeDetailsList = new EmployeeDetailsList();
    List<EmployeeEty> employees;

    Optional<String> searchName = Optional.ofNullable(name);

    if (searchName.isPresent() && !searchName.get().isEmpty()) {
      employees = employeeRepository.findAll().stream()
          .filter(employee ->
              employee.getFirstName().toLowerCase().contains(searchName.get().toLowerCase()) ||
              employee.getLastName().toLowerCase().contains(searchName.get().toLowerCase()))
          .collect(Collectors.toList());
    } else {
      employees = employeeRepository.findAll();
    }

    employeeDetailsList.setItems(employees.stream()
        .map(EmployeeMapper.INSTANCE::mapEmployeeEtyToEmployeeDto)
        .collect(Collectors.toList()));

    return employeeDetailsList;
  }

  Integer getTotalYearlyDaysOffFromEmployee(EmployeeEty employee) {
    Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
    //  stream that returns an employee's total yearly days off (from the current year)
    return employee.getEmpYearlyDaysOff()
            .stream().filter(daysOffEntry -> daysOffEntry.getYear().equals(currentYear))
            .findFirst().map(EmpYearlyDaysOffEty::getTotalNoDays)
            .orElseThrow(() -> new BusinessException(BusinessException.BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.YEARLY_DAYS_OFF_NOT_SET).build()));
  }

  List<LeaveRequestEty> getVacationLeaveRequests(EmployeeEty employee) {
    //  stream that returns an employee's leave requests that are considered to use days off (only VACATION marked ones that aren't REJECTED)
    return employee.getLeaveRequests().stream()
            .filter(request -> request.getType().equals(LeaveRequestEtyTypeEnum.VACATION)
                    && !(request.getStatus().equals(LeaveRequestEtyStatusEnum.REJECTED))).toList();
  }

  public RemainingDaysOff getEmployeeRemainingDaysOff(String employeeId) {
    var remainingDaysOff = new RemainingDaysOff();
    EmployeeEty employee;

    employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException(BusinessException.BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()));

    Integer totalDaysOff = getTotalYearlyDaysOffFromEmployee(employee);

    List<LeaveRequestEty> approvedVacationLeaveRequests = getVacationLeaveRequests(employee);

    Integer spentDaysOff;
    //  we assume that we can take the spent days off from the leave requests directly without checking for valid dates
    //  i.e. weekends/legal days off (validated on data input)
    spentDaysOff = approvedVacationLeaveRequests.stream().mapToInt(LeaveRequestEty::getNoDays).reduce(0, Integer::sum);
    remainingDaysOff.setRemainingDays(totalDaysOff - spentDaysOff);
    return remainingDaysOff;
  }

  public void checkEmployeeUniqueCredentials(String usernameParam, String emailParam) {
    Optional<String> username = Optional.ofNullable(usernameParam);
    Optional<String> email = Optional.ofNullable(emailParam);

    if (username.isPresent() && !username.get().isEmpty()) {
      if (employeeRepository.findByUsername(username.get()).size() > 0)
        throw new BusinessException(BusinessException.BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.USERNAME_DUPLICATE).build());
    }
    if (email.isPresent() && !email.get().isEmpty()) {
      if (employeeRepository.findByEmail(email.get()).size() > 0)
        throw new BusinessException(BusinessException.BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.EMAIL_DUPLICATE).build());
    }
  }

  public void inactivateEmployee(String employeeId){

    EmployeeEty employee = employeeRepository.findById(employeeId).orElseThrow(
        () -> new BusinessException(BusinessExceptionElement
        .builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()));

      employee.setStatus("INACTIVE");

      employee.setMdfTms(Instant.now());

      employee.setMdfUsr("User"); //todo change when login ready

      employeeRepository.save(employee);
  }

  @Transactional
  public LeaveRequestDetailsListItem editLeaveRequest(String employeeId,
      Long requestId,
      EditLeaveRequestDetails editLeaveRequestDetails){

    EmployeeEty employee = employeeRepository.findById(employeeId).orElseThrow(
        () -> new BusinessException(BusinessExceptionElement.builder().errorDescription(
            BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()
        ));

    LeaveRequestEty leaveRequest = checkLeaveRequestExists(employee, requestId);

    validateLeaveRequest(leaveRequest, editLeaveRequestDetails);

    if(leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.APPROVED) ||
        leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.PENDING)){

      leaveRequest.setStartDate(editLeaveRequestDetails.getStartDate());
      leaveRequest.setEndDate(editLeaveRequestDetails.getEndDate());
      leaveRequest.setType(editLeaveRequestDetails.getType());
      leaveRequest.setDescription(editLeaveRequestDetails.getDescription());
      leaveRequest.setStatus(LeaveRequestEtyStatusEnum.PENDING);
    }

    LeaveRequestDetailsListItem leaveRequestDetailsListItem = LeaveRequestMapper.INSTANCE
        .mapLeaveRequestEtyToLeaveRequestDto(leaveRequestRepository.save(leaveRequest));

    return leaveRequestDetailsListItem;
  }

  private LeaveRequestEty checkLeaveRequestExists(EmployeeEty employee, Long requestId){

    return employee.getLeaveRequests().stream()
        .filter(leaveRequestEty -> leaveRequestEty.getId().equals(requestId)).findFirst()
        .orElseThrow(() -> new BusinessException(BusinessExceptionElement.builder()
            .errorDescription(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND).build()));

  }
  private void validateLeaveRequest(LeaveRequestEty leaveRequest, EditLeaveRequestDetails editLeaveRequestDetails){

    if(editLeaveRequestDetails.getV() < leaveRequest.getV()){
      throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
          BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION).build());
    }
    if(leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.REJECTED)){
      throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
          BusinessErrorCode.LEAVE_REQUEST_REJECTED).build());
    }
    if(editLeaveRequestDetails.getStartDate().isBefore(LocalDate.now().withDayOfMonth(1))){
      throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
          BusinessErrorCode.LEAVE_REQUEST_PAST_DATE).build());
    }
  }
}
