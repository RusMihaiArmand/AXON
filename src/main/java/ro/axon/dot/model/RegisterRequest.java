package ro.axon.dot.model;

import java.time.LocalDate;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
  @NotEmpty
  private String username;

  @NotEmpty
  private String firstname;

  @NotEmpty
  private String lastname;

  @NotEmpty
  private String email;

  @NotEmpty
  private String role;

  @NotNull
  private Long teamId;

  @NotNull
  private LocalDate contractStartDate;

  @NotNull
  @Min(value = 0, message = "Numbers of days cannot be negative")
  private Integer noDaysOff;
}
