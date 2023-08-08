package ro.axon.dot.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.EmployeeDetailsList;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;

  private final PasswordEncoder passwordEncoder;

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
  public EmployeeDetailsListItem createEmployee(EmployeeDetailsListItem employee) {

    verifyEmployeeExists(employee.getUsername());

    EmployeeEty toSave = EmployeeMapper.INSTANCE.mapEmployeeDtoToEmployeeEty(employee);
    toSave.setPassword(passwordEncoder.encode("axon_" + toSave.getUsername()));

    EmployeeEty saved = employeeRepository.save(toSave);

    return EmployeeMapper.INSTANCE.mapEmployeeEtyToEmployeeDto(saved);
  }

  public EmployeeEty loadEmployeeByUsername(String username) {

    return employeeRepository.findEmployeeByUsername(username)
        .orElseThrow(() ->
            new BusinessException(BusinessExceptionElement
                .builder()
                .errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND)
                .build()
            ));
  }

  private void verifyEmployeeExists(String username) {

    if (employeeRepository.findEmployeeByUsername(username).isPresent()) {
      throw new BusinessException(BusinessExceptionElement
          .builder()
          .errorDescription(BusinessErrorCode.EMPLOYEE_ALREADY_EXISTS)
          .build());
    }
  }

}
