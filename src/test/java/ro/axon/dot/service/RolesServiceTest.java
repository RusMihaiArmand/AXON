package ro.axon.dot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.axon.dot.config.EmployeeRolesProperties;
import ro.axon.dot.model.EmployeeRolesList;

@ExtendWith(MockitoExtension.class)
class RolesServiceTest {

  @Mock
  EmployeeRolesProperties employeeRolesProperties;
  @InjectMocks
  RolesService rolesService;

  @Test
  @DisplayName("When get employee roles from properties then return roles")
  void whenGetRolesListThenReturnRoles() {

    EmployeeRolesList employeeRolesList = new EmployeeRolesList();
    employeeRolesList.setRoles(List.of("USER", "HR", "TEAM_LEAD"));

    when(employeeRolesProperties.roles()).thenReturn(employeeRolesList.getRoles());

    assertThat(rolesService.getEmployeeRoles()).isEqualTo(employeeRolesList);

  }

  @Test
  @DisplayName("When get employee roles from properties then return null")
  void whenGetRolesListThenReturnNull() {

    when(employeeRolesProperties.roles()).thenReturn(null);

    assertThat(rolesService.getEmployeeRoles().getRoles()).isNull();

  }

}