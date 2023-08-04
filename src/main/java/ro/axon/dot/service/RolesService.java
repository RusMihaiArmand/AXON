package ro.axon.dot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import ro.axon.dot.config.EmployeeRolesProperties;
import ro.axon.dot.model.EmployeeRolesList;

@Service
@EnableConfigurationProperties(EmployeeRolesProperties.class)
@RequiredArgsConstructor

public class RolesService {

  private final EmployeeRolesProperties employeeRolesProperties;

  public EmployeeRolesList getEmployeeRoles() {
    EmployeeRolesList employeeRolesList = new EmployeeRolesList();
    employeeRolesList.setRoles(employeeRolesProperties.roles());
    return employeeRolesList;
  }

}
