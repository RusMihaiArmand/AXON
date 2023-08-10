package ro.axon.dot.service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.*;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.model.EmployeeDetailsList;
import ro.axon.dot.model.RemainingDaysOff;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;

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

  public RemainingDaysOff getEmployeeRemainingDaysOff(String employeeId) {
    var remainingDaysOff = new RemainingDaysOff();
    EmployeeEty employee;

    employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new BusinessException(BusinessException.BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()));

    Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
    Integer totalDaysOff = employee.getEmpYearlyDaysOff()
            .stream().filter(daysOffEntry -> daysOffEntry.getYear().equals(currentYear))
            .findFirst().map(EmpYearlyDaysOffEty::getTotalNoDays)
            .orElseThrow(() -> new BusinessException(BusinessException.BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.YEARLY_DAYS_OFF_NOT_SET).build()));

    List<LeaveRequestEty> approvedVacationLeaveRequests = employee.getLeaveRequests().stream()
            .filter(request -> request.getType().equals(LeaveRequestEtyTypeEnum.VACATION) && (request.getStatus().equals(LeaveRequestEtyStatusEnum.PENDING) || request.getStatus().equals(LeaveRequestEtyStatusEnum.APPROVED))).toList();

    Integer spentDaysOff;

    spentDaysOff = approvedVacationLeaveRequests.stream().mapToInt(LeaveRequestEty::getNoDays).reduce(0, Integer::sum);
    remainingDaysOff.setRemainingDays(totalDaysOff - spentDaysOff);
    return remainingDaysOff;
  }

}
