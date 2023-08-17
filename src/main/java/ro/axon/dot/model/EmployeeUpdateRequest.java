package ro.axon.dot.model;

import lombok.Data;
import lombok.NonNull;

@Data
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