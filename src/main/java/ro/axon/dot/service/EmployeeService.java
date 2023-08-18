package ro.axon.dot.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.EmpYearlyDaysOffHistEty;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.LeaveRequestEty;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;
import ro.axon.dot.domain.LeaveRequestRepository;
import ro.axon.dot.domain.VacationDaysChangeTypeEnum;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.domain.TeamRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.mapper.LeaveRequestMapper;
import ro.axon.dot.model.CreateLeaveRequestDetails;
import ro.axon.dot.model.LeaveRequestCreateEditDetails;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsListItem;
import ro.axon.dot.model.LeaveRequestReview;
import ro.axon.dot.model.LegallyDaysOffItem;
import ro.axon.dot.model.RegisterRequest;
import ro.axon.dot.model.RemainingDaysOff;
import ro.axon.dot.model.VacationDaysModifyDetails;
import ro.axon.dot.security.JwtTokenUtil;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final TeamRepository teamRepository;
  private final LeaveRequestRepository leaveRequestRepository;
  private final LegallyDaysOffService legallyDaysOffService;

  private final PasswordEncoder passwordEncoder;
  private final JwtTokenUtil tokenUtil;

  @Transactional
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

    employee.setMdfUsr(tokenUtil.getLoggedUserId());

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

      int countedDaysOff = checkCountedDaysOff(editLeaveRequestDetails, employee);

      leaveRequest = setLeaveRequestFromDTO(leaveRequest, editLeaveRequestDetails, countedDaysOff);
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

    EmployeeEty employee = findEmployeeById(employeeId);

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

    int countedDaysOff = checkCountedDaysOff(createLeaveRequestDetails, employee);

    LeaveRequestEty leaveRequestEty = new LeaveRequestEty();

    leaveRequestEty = setLeaveRequestFromDTO(leaveRequestEty, createLeaveRequestDetails, countedDaysOff);

    employee.addLeaveRequest(leaveRequestEty);
    employeeRepository.save(employee);

  }

  private LeaveRequestEty setLeaveRequestFromDTO(LeaveRequestEty leaveRequestEty,
      LeaveRequestCreateEditDetails leaveRequestDTO, int countedDaysOff){
    
    leaveRequestEty.setNoDays(countedDaysOff);
    leaveRequestEty.setStartDate(leaveRequestDTO.getStartDate());
    leaveRequestEty.setEndDate(leaveRequestDTO.getEndDate());
    leaveRequestEty.setType(leaveRequestDTO.getType());
    leaveRequestEty.setDescription(leaveRequestDTO.getDescription());
    leaveRequestEty.setStatus(LeaveRequestEtyStatusEnum.PENDING);

    leaveRequestEty.setCrtUsr(tokenUtil.getLoggedUserId());
    leaveRequestEty.setCrtTms(Instant.now());
    leaveRequestEty.setMdfUsr(tokenUtil.getLoggedUserId());
    leaveRequestEty.setMdfTms(Instant.now());

    return leaveRequestEty;
  }

  private int checkCountedDaysOff(LeaveRequestCreateEditDetails leaveRequestDate, EmployeeEty employee){

    long countedDaysOff = calculateCountedDaysOff(leaveRequestDate.getStartDate(),
        leaveRequestDate.getEndDate());

    Integer remainingDaysOff = getCalculatedRemainingDaysOff(employee);

    if (countedDaysOff > remainingDaysOff) {
      throw new BusinessException(
          BusinessException.BusinessExceptionElement
              .builder()
              .errorDescription(BusinessErrorCode.LEAVE_RQST_INVALID_NUMBER_DAYS)
              .build());
    }

    return (int)countedDaysOff;
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

    empDaysOffEty.setEmployee(employee);
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

    daysOffHistory.setCrtUsr(tokenUtil.getLoggedUserId());
    daysOffHistory.setCrtTms(Instant.now());

    daysOffHistory.setEmpYearlyDaysOffEty(daysOffEty);

    daysOffEty.getEmpYearlyDaysOffHistEtySet().add(daysOffHistory);

    employeeRepository.save(emp);
  }

  @Transactional
  public EmployeeDetailsListItem createEmployee(RegisterRequest request, String loggedUserId) {

    verifyEmployeeExists(request.getUsername());

    TeamEty team = loadTeamById(request.getTeamId());
    final LocalDateTime now = LocalDateTime.now();

    EmployeeEty toSave = setEmployeeDetails(request, loggedUserId, team, now);
    EmpYearlyDaysOffEty daysOff = setDaysOffDetails(request, loggedUserId, toSave, now);

    toSave.setEmpYearlyDaysOff(Set.of(daysOff));
    team.getEmployees().add(toSave);

    EmployeeEty saved = employeeRepository.save(toSave);
    return EmployeeMapper.INSTANCE.mapEmployeeEtyToEmployeeDto(saved);
  }

  private EmpYearlyDaysOffEty setDaysOffDetails(RegisterRequest request, String loggedUserId, EmployeeEty employee, LocalDateTime now){
    EmpYearlyDaysOffEty daysOff = new EmpYearlyDaysOffEty();

    daysOff.setYear(request.getContractStartDate().getYear());
    daysOff.setTotalNoDays(request.getNoDaysOff());

    EmpYearlyDaysOffHistEty daysOffHistEty = new EmpYearlyDaysOffHistEty();
    daysOffHistEty.setEmpYearlyDaysOffEty(daysOff);
    daysOffHistEty.setNoDays(request.getNoDaysOff());
    daysOffHistEty.setDescription("Initial number of days off for the current year");
    daysOffHistEty.setType(VacationDaysChangeTypeEnum.INCREASE);
    daysOffHistEty.setCrtUsr(loggedUserId);
    daysOffHistEty.setCrtTms(now.toInstant(ZoneOffset.UTC));

    daysOff.setEmpYearlyDaysOffHistEtySet(Set.of(daysOffHistEty));
    daysOff.setEmployee(employee);

    return daysOff;
  }
  private EmployeeEty setEmployeeDetails(RegisterRequest request, String loggedUserId, TeamEty team, LocalDateTime now){
    EmployeeEty toSave = new EmployeeEty();

    toSave.setTeam(team);
    toSave.setFirstName(request.getFirstname());
    toSave.setLastName(request.getLastname());
    toSave.setUsername(request.getUsername());
    toSave.setTeam(team);
    toSave.setRole(request.getRole());
    toSave.setEmail(request.getEmail());
    toSave.setContractStartDate(request.getContractStartDate());
    toSave.setCrtUsr(loggedUserId);
    toSave.setMdfUsr(loggedUserId);
    toSave.setCrtTms(now.toInstant(ZoneOffset.UTC));
    toSave.setMdfTms(now.toInstant(ZoneOffset.UTC));
    toSave.setStatus("ACTIVE");
    toSave.setPassword(passwordEncoder.encode("axon_" + toSave.getUsername()));

    return toSave;
  }

  public TeamEty loadTeamById(Long id){
    return teamRepository.findById(id)
        .orElseThrow(() -> new BusinessException(
            BusinessExceptionElement
                .builder()
                .errorDescription(BusinessErrorCode.TEAM_NOT_FOUND)
                .build())
        );
  }

  public EmployeeEty loadEmployeeByUsername(String username) {
    return employeeRepository.findEmployeeByUsername(username)
        .orElseThrow(() -> new BusinessException(
            BusinessExceptionElement
                .builder()
                .errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND)
                .build()
        ));
  }

  public EmployeeEty loadEmployeeById(String id) {
    return employeeRepository.findById(id)
        .orElseThrow(() -> new BusinessException(
            BusinessExceptionElement
                .builder()
                .errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND)
                .build()
        ));
  }


  private void verifyEmployeeExists(String username) {
    if (employeeRepository.existsByUsername(username)) {
      throw new BusinessException(BusinessExceptionElement
          .builder()
          .errorDescription(BusinessErrorCode.USERNAME_DUPLICATE)
          .build());
    }
  }

}
