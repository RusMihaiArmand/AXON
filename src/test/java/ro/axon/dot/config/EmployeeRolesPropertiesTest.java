package ro.axon.dot.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ro.axon.dot.config.properties.EmployeeRolesProperties;

@EnableConfigurationProperties(EmployeeRolesProperties.class)
class EmployeeRolesPropertiesTest {

  private final ApplicationContextRunner context = new ApplicationContextRunner().withUserConfiguration(
      EmployeeRolesPropertiesTest.class);

  @Test
  @DisplayName("When get roles from properties then return roles")
  void whenGetRolesFromPropertiesThenReturnRoles() {
    context.withPropertyValues("ro.axon.core.roles=USER, HR, TEAM_LEAD")
        .run(context -> assertThat(context.getBean(EmployeeRolesProperties.class).roles(),
            is(List.of("USER", "HR", "TEAM_LEAD"))));
  }

  @Test
  @DisplayName("When get roles from properties then return empty")
  void whenGetRolesFromPropertiesThenReturnEmpty() {
    context.withPropertyValues("ro.axon.core.roles=")
        .run(context -> assertThat(context.getBean(EmployeeRolesProperties.class).roles(),
            is(List.of())));
  }

}