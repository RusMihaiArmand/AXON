package ro.axon.dot.model;

import java.time.LocalDate;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
  @NotEmpty(message = "Username should not be empty or null")
  private String username;

  @NotEmpty(message = "Firstname should not be empty or null")
  private String firstname;

  @NotEmpty(message = "Lastname should not be empty or null")
  private String lastname;

  @NotEmpty(message = "Email should not be empty or null")
  private String email;

  @NotEmpty(message = "Role should not be empty or null")
  private String role;

  @NotNull(message = "Team is missing")
  private Long teamId;

  @NotNull(message = "Contract start date should not be null")
  private LocalDate contractStartDate;

  @NotNull(message = "Number of days off should not be null")
  @Min(value = 0, message = "Numbers of days cannot be negative")
  private Integer noDaysOff;
}
