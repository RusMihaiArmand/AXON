package ro.axon.dot.service;

import org.springframework.stereotype.Service;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessExceptionElement;

import ro.axon.dot.model.EmployeeDto;
import ro.axon.dot.domain.EmployeeRepository;


@Service
public class UpdateEmployeeService {

  private final EmployeeRepository employeeRepository;

  public UpdateEmployeeService(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  public void updateEmployeeDetails(String employeeId, EmployeeDto employeeDto) {
    EmployeeEty employeeEty = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new BusinessException(
            new BusinessExceptionElement(
                BusinessErrorCode.EMPLOYEE_NOT_FOUND,
                null
            )
        ));

    if (employeeDto.getV() < employeeEty.getV()) {
      throw new BusinessException(
          new BusinessExceptionElement(
              BusinessErrorCode.CONFLICT,
              null
          )
      );
    }

    employeeEty.setFirstName(employeeDto.getFirstName());
    employeeEty.setLastName(employeeDto.getLastName());
    employeeEty.setEmail(employeeDto.getEmail());
    employeeEty.setRole(employeeDto.getRole());

    

    employeeRepository.save(employeeEty);
  }
}
