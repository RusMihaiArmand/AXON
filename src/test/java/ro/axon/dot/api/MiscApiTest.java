package ro.axon.dot.api;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.axon.dot.model.EmployeeRolesList;
import ro.axon.dot.service.RolesService;

@ExtendWith(MockitoExtension.class)
class MiscApiTest {
  @Mock
  RolesService roleService;
  @InjectMocks
  MiscApi miscApi;
  MockMvc mockMvc;
  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(miscApi).build();
  }

  @Test
  @DisplayName("When get employee roles from properties then return status ok")
  void whenGetRolesThenReturnStatusOk() throws Exception {

    EmployeeRolesList employeeRolesList = new EmployeeRolesList();
    employeeRolesList.setRoles(List.of("USER", "HR", "TEAM_LEAD"));

    when(roleService.getEmployeeRoles()).thenReturn(employeeRolesList);

    mockMvc.perform(get("/api/v1/misc/roles")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roles", equalTo(List.of("USER", "HR", "TEAM_LEAD"))));
  }

  @Test
  @DisplayName("When get employee roles from properties then return null")
  void whenGetRolesThenReturnNull() throws Exception {

    when(roleService.getEmployeeRoles()).thenReturn(null);

    mockMvc.perform(get("/api/v1/misc/roles")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").doesNotExist());
  }

}