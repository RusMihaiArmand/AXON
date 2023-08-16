package ro.axon.dot.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.EmpYearlyDaysOffHistEty;
import ro.axon.dot.domain.EmpYearlyDaysOffHistRepository;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.LeaveRequestEty;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;
import ro.axon.dot.domain.LeaveRequestRepository;
import ro.axon.dot.domain.VacationDaysChangeTypeEnum;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.mapper.LeaveRequestMapper;
import ro.axon.dot.model.CreateLeaveRequestDetails;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsListItem;
import ro.axon.dot.model.LeaveRequestReview;
import ro.axon.dot.model.LegallyDaysOffItem;
import ro.axon.dot.model.RemainingDaysOff;
import ro.axon.dot.model.VacationDaysModifyDetails;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final LeaveRequestRepository leaveRequestRepository;
  private final EmpYearlyDaysOffHistRepository empYearlyDaysOffHistRepository;
  private final LegallyDaysOffService legallyDaysOffService;

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

  private Integer getTotalYearlyDaysOffFromEmployee(EmployeeEty employee) {
    Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
    //  stream that returns an employee's total yearly days off (from the current year)
    return employee.getEmpYearlyDaysOff()
        .stream().filter(daysOffEntry -> daysOffEntry.getYear().equals(currentYear))
        .findFirst().map(EmpYearlyDaysOffEty::getTotalNoDays)
        .orElseThrow(() -> new BusinessException(
            BusinessException.BusinessExceptionElement.builder()
                .errorDescription(BusinessErrorCode.YEARLY_DAYS_OFF_NOT_SET).build()));
  }

  private List<LeaveRequestEty> getVacationLeaveRequests(EmployeeEty employee) {
    //  stream that returns an employee's leave requests that are considered to use days off (only VACATION marked ones that aren't REJECTED)
    return employee.getLeaveRequests().stream()
        .filter(request -> request.getType().equals(LeaveRequestEtyTypeEnum.VACATION)
            && !(request.getStatus().equals(LeaveRequestEtyStatusEnum.REJECTED))).toList();
  }

  private void checkEmployeeExists(Long idEmployee) throws BusinessException {
    Optional<EmployeeEty> employeeOptional = employeeRepository.findById(
        String.valueOf(idEmployee));
    if (employeeOptional.isEmpty()) {
      throw new BusinessException(
          BusinessException.BusinessExceptionElement
              .builder()
              .errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND)
              .build());
    }
  }

  private LeaveRequestEty checkLeaveRequestExists(Long idRequest) throws BusinessException {
    Optional<LeaveRequestEty> requestOptional = leaveRequestRepository.findById(idRequest);
    if (requestOptional.isEmpty()) {
      throw new BusinessException(
          BusinessException.BusinessExceptionElement
              .builder()
              .errorDescription(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND)
              .build());
    }
    return requestOptional.get();
  }

  private void checkVersion(LeaveRequestEty request, LeaveRequestReview review)
      throws BusinessException {
    if (review.getVersion() < request.getV()) {
      throw new BusinessException(
          BusinessException.BusinessExceptionElement
              .builder()
              .errorDescription(BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION)
              .build());
    }
  }

  private void checkStatus(LeaveRequestEty request) {
    if (request.getStatus() != LeaveRequestEtyStatusEnum.PENDING) {
      throw new BusinessException(
          BusinessException.BusinessExceptionElement
              .builder()
              .errorDescription(BusinessErrorCode.LEAVE_REQUEST_REJECTED)
              .build());
    }
  }

  public LeaveRequestEty updateLeaveRequestStatus(Long idEmployee, Long idRequest,
      LeaveRequestReview review) throws BusinessException {
    checkEmployeeExists(idEmployee);
    LeaveRequestEty request = checkLeaveRequestExists(idRequest);
    checkVersion(request, review);
    checkStatus(request);

    request.setStatus(LeaveRequestEtyStatusEnum.valueOf(review.getLeaveRequestStatus()));
    request.setRejectReason(review.getRejectReason());
    request.setV(review.getVersion());
    leaveRequestRepository.save(request);
    return request;
  }


  private boolean isUsernameFound(String usernameParam, EmployeeRepository employeeRepository) {
    Optional<String> username = Optional.ofNullable(usernameParam);
    if (username.isPresent() && !username.get().isEmpty()) {
      return employeeRepository.existsByUsername(username.get());
    }
    return false;
  }

  private boolean isEmailFound(String emailParam, EmployeeRepository employeeRepository) {
    Optional<String> email = Optional.ofNullable(emailParam);
    if (email.isPresent() && !email.get().isEmpty()) {
      return employeeRepository.existsByEmail(email.get());
    }
    return false;
  }

  public boolean checkEmployeeUniqueCredentials(String usernameParam, String emailParam) {

    if (isUsernameFound(usernameParam, employeeRepository)) {
      throw new BusinessException(BusinessException.BusinessExceptionElement.builder()
          .errorDescription(BusinessErrorCode.USERNAME_DUPLICATE).build());
    }
    if (isEmailFound(emailParam, employeeRepository)) {
      throw new BusinessException(BusinessException.BusinessExceptionElement.builder()
          .errorDescription(BusinessErrorCode.EMAIL_DUPLICATE).build());
    }
    return true;
  }

  public void inactivateEmployee(String employeeId) {

    EmployeeEty employee = findEmployeeById(employeeId);

    employee.setStatus("INACTIVE");

    employee.setMdfTms(Instant.now());

    employee.setMdfUsr("User"); //todo change when login ready

    employeeRepository.save(employee);
  }

  @Transactional
  public LeaveRequestDetailsListItem editLeaveRequest(String employeeId,
      Long requestId,
      EditLeaveRequestDetails editLeaveRequestDetails) {

    EmployeeEty employee = findEmployeeById(employeeId);

    LeaveRequestEty leaveRequest = checkLeaveRequestExists(employee, requestId);

    validateLeaveRequest(leaveRequest, editLeaveRequestDetails);

    if (isPendingOrApprovedLeaveRequest(leaveRequest)) {

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

  @Transactional
  public void deleteLeaveRequest(String employeeId, Long requestId) {

    EmployeeEty employee = findEmployeeById(employeeId);

    LeaveRequestEty leaveRequest = checkLeaveRequestExists(employee, requestId);

    if (leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.REJECTED)) {

      throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
          BusinessErrorCode.LEAVE_REQUEST_REJECTED).build());
    }
    if (leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.APPROVED) &&
        leaveRequest.getStartDate().isBefore(LocalDate.now().withDayOfMonth(1))) {

      throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
          BusinessErrorCode.LEAVE_REQUEST_DELETE_APPROVED_PAST_DATE).build());
    }

    if (isPendingOrApprovedLeaveRequest(leaveRequest)) {

      employee.removeLeaveRequests(leaveRequest);
      employeeRepository.save(employee);
    }
  }

  private EmployeeEty findEmployeeById(String employeeId) {

    return employeeRepository.findById(employeeId).orElseThrow(
        () -> new BusinessException(BusinessExceptionElement.builder().errorDescription(
            BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()
        ));
  }

  private LeaveRequestEty checkLeaveRequestExists(EmployeeEty employee, Long requestId) {

    return employee.getLeaveRequests().stream()
        .filter(leaveRequestEty -> leaveRequestEty.getId().equals(requestId)).findFirst()
        .orElseThrow(() -> new BusinessException(BusinessExceptionElement.builder()
            .errorDescription(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND).build()));

  }

  private void validateLeaveRequest(LeaveRequestEty leaveRequest, EditLeaveRequestDetails
      editLeaveRequestDetails) {

    if (editLeaveRequestDetails.getV() < leaveRequest.getV()) {
      throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
          BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION).build());
    }
    if (leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.REJECTED)) {
      throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
          BusinessErrorCode.LEAVE_REQUEST_REJECTED).build());
    }
    if (editLeaveRequestDetails.getStartDate().isBefore(LocalDate.now().withDayOfMonth(1))) {
      throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
          BusinessErrorCode.LEAVE_REQUEST_PAST_DATE).build());
    }
  }

  private boolean isPendingOrApprovedLeaveRequest(LeaveRequestEty leaveRequest) {

    return leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.PENDING) ||
        leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.APPROVED);
  }

  public Integer getCalculatedRemainingDaysOff(EmployeeEty employee) {

    Integer totalDaysOff = getTotalYearlyDaysOffFromEmployee(employee);
    List<LeaveRequestEty> approvedVacationLeaveRequests = getVacationLeaveRequests(employee);
    Integer spentDaysOff;
    //  we assume that we can take the spent days off from the leave requests directly without checking for valid dates
    //  i.e. weekends/legal days off (validated on data input)
    spentDaysOff = approvedVacationLeaveRequests.stream().mapToInt(LeaveRequestEty::getNoDays)
        .reduce(0, Integer::sum);
    return totalDaysOff - spentDaysOff;

  }

  public RemainingDaysOff getEmployeeRemainingDaysOff(String employeeId) {
    var remainingDaysOff = new RemainingDaysOff();
    EmployeeEty employee;

    employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new BusinessException(
            BusinessException.BusinessExceptionElement.builder()
                .errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()));

    remainingDaysOff.setRemainingDays(getCalculatedRemainingDaysOff(employee));

    return remainingDaysOff;

  }

  @Transactional
  public void createLeaveRequest(String employeeId,
      CreateLeaveRequestDetails createLeaveRequestDetails) {

    EmployeeEty employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new BusinessException(BusinessExceptionElement
            .builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()));

    if (createLeaveRequestDetails.getEndDate()
        .isBefore(createLeaveRequestDetails.getStartDate())) {
      throw new BusinessException(
          BusinessException.BusinessExceptionElement
              .builder()
              .errorDescription(BusinessErrorCode.LEAVE_RQST_INVALID_PERIOD)
              .build());
    }

    if (createLeaveRequestDetails.getStartDate().getYear()
        != createLeaveRequestDetails.getEndDate().getYear()) {
      throw new BusinessException(
          BusinessException.BusinessExceptionElement
              .builder()
              .errorDescription(BusinessErrorCode.LEAVE_RQST_DIFF_YEARS)
              .build());

    }

    var currentDate = LocalDate.now();

    if (createLeaveRequestDetails.getStartDate().getMonthValue()
        < currentDate.getMonthValue()) {
      throw new BusinessException(
          BusinessException.BusinessExceptionElement
              .builder()
              .errorDescription(BusinessErrorCode.LEAVE_RQST_INVALID_MONTH)
              .build());
    }

    long countedDaysOff = calculateCountedDaysOff(createLeaveRequestDetails.getStartDate(),
        createLeaveRequestDetails.getEndDate());

    Integer remainingDaysOff = getCalculatedRemainingDaysOff(employee);

    if (countedDaysOff > remainingDaysOff) {
      throw new BusinessException(
          BusinessException.BusinessExceptionElement
              .builder()
              .errorDescription(BusinessErrorCode.LEAVE_RQST_INVALID_NUMBER_DAYS)
              .build());
    }

    LeaveRequestEty leaveRequestEty = new LeaveRequestEty();

    leaveRequestEty.setNoDays((int) countedDaysOff);
    leaveRequestEty.setStartDate(createLeaveRequestDetails.getStartDate());
    leaveRequestEty.setEndDate(createLeaveRequestDetails.getEndDate());
    leaveRequestEty.setType(createLeaveRequestDetails.getType());
    leaveRequestEty.setDescription(createLeaveRequestDetails.getDescription());

    leaveRequestEty.setCrtUsr("user");    //todo to be modified when login endpoint is available
    leaveRequestEty.setCrtTms(Instant.now());
    leaveRequestEty.setMdfUsr("user");
    leaveRequestEty.setMdfTms(Instant.now());
    leaveRequestEty.setStatus(LeaveRequestEtyStatusEnum.PENDING);

    employee.addLeaveRequest(leaveRequestEty);
    employeeRepository.save(employee);

  }

  protected long calculateCountedDaysOff(LocalDate startDate, LocalDate endDate) {
    return startDate.datesUntil(endDate.plusDays(1))
        .filter(date -> isWeekDay(date) && isNotLegallyDayOff(date))
        .count();
  }

  private boolean isWeekDay(LocalDate date) {
    DayOfWeek dayOfWeek = date.getDayOfWeek();
    return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
  }

  private boolean isNotLegallyDayOff(LocalDate date) {
    List<LegallyDaysOffItem> legallyDaysOffList = legallyDaysOffService.getAllLegallyOffDays()
        .getDays();
    return legallyDaysOffList.stream()
        .noneMatch(offDay -> offDay.getDate().equals(date));
  }

  public LeaveRequestDetailsList getLeaveRequests(String idEmployee, LocalDate startDate,
      LocalDate endDate) {
    Optional<EmployeeEty> employeeOptional = employeeRepository.findById(idEmployee);
    if (employeeOptional.isEmpty()) {
      throw new BusinessException(
          BusinessException.BusinessExceptionElement
              .builder()
              .errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND)
              .build());
    }

    List<LeaveRequestEty> leaveRequests = employeeOptional.get().getLeaveRequests().stream()
        .toList();
    if (startDate != null) {
      leaveRequests = leaveRequests
          .stream()
          .filter(request ->
              startDate.compareTo(request.getStartDate()) <= 0
                  && request.getEndDate().compareTo(endDate) <= 0)
          .collect(Collectors.toList());
    }

    LeaveRequestDetailsList leaveRequestsDTO = new LeaveRequestDetailsList();
    leaveRequestsDTO.setItems(leaveRequests
        .stream()
        .map(LeaveRequestMapper.INSTANCE::mapLeaveRequestEtyToLeaveRequestDto)
        .collect(Collectors.toList()));

    return leaveRequestsDTO;
  }


  private int getDaysToModify(VacationDaysModifyDetails vacationDaysModifyDetails)
  {
    if(vacationDaysModifyDetails.getType().equals(VacationDaysChangeTypeEnum.DECREASE))
      return -vacationDaysModifyDetails.getNoDays();
    else {
      return vacationDaysModifyDetails.getNoDays();
    }
  }

  @Transactional
  public void changeVacationDays(VacationDaysModifyDetails vacationDaysModifyDetails)
  {
    int dayChanger = this.getDaysToModify(vacationDaysModifyDetails);

    employeeRepository.findAllById(vacationDaysModifyDetails.getEmployeeIds())
        .forEach(employee -> updateDaysForEmployee(dayChanger,vacationDaysModifyDetails.getDescription(),employee));
  }

  EmpYearlyDaysOffEty createDaysOffEty(EmployeeEty employee){
    EmpYearlyDaysOffEty empDaysOffEty = new EmpYearlyDaysOffEty();

    empDaysOffEty.setEmployeeEty(employee);
    empDaysOffEty.setYear( LocalDate.now().getYear() );
    empDaysOffEty.setTotalNoDays(0);
    empDaysOffEty.setEmpYearlyDaysOffHistEtySet( new HashSet<>() );

    employee.getEmpYearlyDaysOff().add(empDaysOffEty);

    return empDaysOffEty;
  }

  private void updateDaysForEmployee(int daysChanger, String description, EmployeeEty emp) throws BusinessException
  {
    EmpYearlyDaysOffEty daysOffEty;
    daysOffEty = emp.getEmpYearlyDaysOff().stream()
        .filter(yearlyDaysOff -> yearlyDaysOff.getYear().equals(LocalDate.now().getYear()))
        .findFirst()
        .orElseGet( () -> createDaysOffEty(emp));

    int daysLeft = daysOffEty.getTotalNoDays() + daysChanger;

    if (daysLeft < 0) {
      throw new BusinessException(BusinessExceptionElement
          .builder().errorDescription(BusinessErrorCode.NEGATIVE_DAYS_OFF).build());
    }

    daysOffEty.setTotalNoDays(daysLeft);
    daysOffEty.setYear( LocalDate.now().getYear() );

    EmpYearlyDaysOffHistEty daysOffHistory = new EmpYearlyDaysOffHistEty();

    daysOffHistory.setNoDays(Math.abs(daysChanger));
    daysOffHistory.setDescription(description);

    if (daysChanger > 0) {
      daysOffHistory.setType(VacationDaysChangeTypeEnum.INCREASE);
    } else {
      daysOffHistory.setType(VacationDaysChangeTypeEnum.DECREASE);
    }

    daysOffHistory.setCrtUsr("CREATION-USER"); //to be modified when login endpoint is finished
    daysOffHistory.setCrtTms(Instant.now());

    daysOffHistory.setEmpYearlyDaysOffEty(daysOffEty);

    daysOffEty.getEmpYearlyDaysOffHistEtySet().add(daysOffHistory);

    employeeRepository.save(emp);
  }
}
