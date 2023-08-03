package ro.axon.dot.service;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.model.EmployeeDetailsList;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;

  public EmployeeDetailsList getEmployeesDetails(){
    var employeeDetailsList = new EmployeeDetailsList();
    employeeDetailsList.setItems(employeeRepository.findAll().stream().map(EmployeeMapper.INSTANCE::mapEmployeeEtyToEmployeeDto)
        .collect(Collectors.toList()));
    return employeeDetailsList;
  }

  public EmployeeDetailsList getEmployeeByName(String name){
    var employeeDetailsList = new EmployeeDetailsList();

    employeeDetailsList.setItems(employeeRepository.findEmployeeByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name).stream()
        .map(EmployeeMapper.INSTANCE::mapEmployeeEtyToEmployeeDto)
        .collect(Collectors.toList()));

    return employeeDetailsList;
  }

}
