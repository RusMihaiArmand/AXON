package ro.axon.dot.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
public class EmployeeUpdateRequest {

  @NotNull(message = "Team is missing")
  private String teamId;
  @NotEmpty(message = "Firstname should not be empty or null")
  private String firstName;
  @NotEmpty(message = "Lastname should not be empty or null")
  private String lastName;
  @NotEmpty(message = "Email should not be empty or null")
  private String email;
  @NotEmpty(message = "Role should not be empty or null")
  private String role;
  @NotNull(message = "Version is missing")
  private Long v;

}