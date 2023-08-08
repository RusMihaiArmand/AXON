package ro.axon.dot.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.model.EmployeeDetailsList;

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

}
