package ro.axon.dot.model;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeUpdateRequest {

  @NotNull
  private String teamId;
  @NotNull
  private String firstName;
  @NotNull
  private String lastName;
  @NotNull
  private String email;
  @NotNull
  private String role;
  @NotNull
  private Long v;

}