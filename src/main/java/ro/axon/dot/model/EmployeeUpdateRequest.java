package ro.axon.dot.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class EmployeeUpdateRequest {

  @NonNull
  private String teamId;
  @NonNull
  private String firstName;
  @NonNull
  private String lastName;
  @NonNull
  private String email;
  @NonNull
  private String role;
  @NonNull
  private Long v;

}