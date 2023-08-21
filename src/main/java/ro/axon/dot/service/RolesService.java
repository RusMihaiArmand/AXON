package ro.axon.dot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.axon.dot.config.properties.EmployeeRolesProperties;
import ro.axon.dot.model.EmployeeRolesList;

@Service
@EnableConfigurationProperties(EmployeeRolesProperties.class)
@RequiredArgsConstructor
public class RolesService {

  private final EmployeeRolesProperties employeeRolesProperties;
  @Transactional(readOnly = true)
  public EmployeeRolesList getEmployeeRoles() {
    EmployeeRolesList employeeRolesList = new EmployeeRolesList();
    employeeRolesList.setRoles(employeeRolesProperties.roles());
    return employeeRolesList;
  }

}
